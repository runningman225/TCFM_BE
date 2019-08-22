package com.future.tcfm.service.impl;
import com.future.tcfm.model.Group;
import com.future.tcfm.model.User;
import com.future.tcfm.model.list.UserContributedList;
import com.future.tcfm.repository.GroupRepository;
import com.future.tcfm.repository.UserRepository;
import com.future.tcfm.service.ExcelReaderService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.omg.SendingContext.RunTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ExcelReaderServiceImpl implements ExcelReaderService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    GroupRepository groupRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public Boolean saveFile(MultipartFile file) {
        try {
            List<Group> groupList = groupRepository.findAllByActive(true);
            List<User> userList = userRepository.findAllByActive(true);
            List<User> newUserList = parseExcelFile(file.getInputStream());
            Map<String,Group> newGroupMap = new HashMap<>();
            for (User user : newUserList) {
                Boolean isGroupExist = false;
                for (User registeredUser : userList){
                    if(user.getEmail().equalsIgnoreCase(registeredUser.getEmail())){
                        throw new RuntimeException("Exception: email already exist!");
                    }
                }
                for (Group group : groupList) {
                    if (user.getGroupName().equalsIgnoreCase(group.getName())) {
                        if(user.getRole().equalsIgnoreCase("GROUP_ADMIN") && group.getGroupAdmin().equalsIgnoreCase("")){
                            group.setGroupAdmin(user.getEmail());
                        }
                        user.setTotalPeriodPayed(group.getCurrentPeriod()-1);
                        isGroupExist = true;
                        break;
                    } else {
                        isGroupExist = false;
                    }
                }
                if (!isGroupExist) {
                    Group newGroup = Group.builder()
                            .groupAdmin("")
                            .name(user.getGroupName())
                            .balanceUsed(0.0)
                            .createdDate(System.currentTimeMillis())
                            .lastModifiedAt(System.currentTimeMillis())
                            .groupBalance(0.0)
                            .bankAccountNumber("")
                            .bankAccountName("")
                            .regularPayment(0.0)
                            .currentPeriod(1)
                            .active(true)
                            .build();
                    newGroupMap.put(newGroup.getName(),newGroup);
                }
            }
            if(newGroupMap.size()>0){
                newGroupMap.forEach((key,value) -> {
                    newUserList.forEach(user -> {
                        if(value.getGroupAdmin().equalsIgnoreCase("") && user.getRole().equalsIgnoreCase("GROUP_ADMIN") && user.getGroupName().equalsIgnoreCase(value.getName())){
                            value.setGroupAdmin(user.getEmail());
                        }
                    });
                    groupList.add(value);
//                    groupRepository.save(value);
                });
            }
            userList.addAll(newUserList);
            userRepository.saveAll(userList);
            groupRepository.saveAll(groupList);
            System.out.println("Bulk Insert succed!");
            return true;
        } catch (IOException e) {
            throw new RuntimeException("Fail -> Message "+e.getMessage());
        }

    }
    @Override
    public ByteArrayInputStream loadFile() throws IOException {

        String[] COLUMNs = {"Email", "Password", "Name", "Phone Number", "Group Name", "Role","NB: There are 2 Roles : 1.MEMBER 2.GROUP_ADMIN"};
        // Create a Workbook
        Workbook workbook = new XSSFWorkbook();     // new HSSFWorkbook() for generating `.xls` file
        ByteArrayOutputStream out = new ByteArrayOutputStream();


        // Create a Sheet
        Sheet sheet = workbook.createSheet("User");


        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.RED.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerCellStyle.setWrapText(true);
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER);

        // Row for Header
        Row headerRow = sheet.createRow(0);
        // Header
        for (int col = 0; col < COLUMNs.length; col++) {
            Cell cell = headerRow.createCell(col);
            cell.setCellValue(COLUMNs[col]);
            cell.setCellStyle(headerCellStyle);
            if(col == 3 || col == 4 || col == COLUMNs.length-1)    sheet.autoSizeColumn(col);
            else sheet.setColumnWidth(col,7500);

        }
        workbook.write(out);
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        return in;
    }

    private List<User> parseExcelFile(InputStream is) {
        try {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheet("User");
            Iterator<Row> rows = sheet.iterator();
            List<User> userList = new ArrayList<>();
//            executor.execute(() -> {
                        while (rows.hasNext()) {
                            Row currentRow = rows.next();
                            System.out.println("Reading row-"+currentRow.getRowNum());
                            // skip row (0)
                            if (currentRow.getRowNum() == 0) {
                                continue;
                            }
                            Iterator<Cell> cellsInRow = currentRow.iterator();
                            User user = new User();
                            int cellIndex = 0;
                            while (cellsInRow.hasNext()) {
                                Cell currentCell = cellsInRow.next();
                                currentCell.setCellType(CellType.STRING);
                                if (cellIndex == 0) { // email
                                    user.setEmail(currentCell.getStringCellValue());
                                } else if (cellIndex == 1) { // password
                                    user.setPassword(currentCell.getStringCellValue());
                                } else if (cellIndex == 2) { // name
                                    user.setName(currentCell.getStringCellValue());
                                } else if (cellIndex == 3) { // phone number
                                    user.setPhone(currentCell.getStringCellValue());
                                } else if (cellIndex == 4) { // groupName
                                    user.setGroupName(currentCell.getStringCellValue());
                                } else if (cellIndex == 5) { // role
                                    user.setRole(currentCell.getStringCellValue());
                                }
                                cellIndex++;
                            }
                            user.setPassword(passwordEncoder.encode(user.getPassword()));
                            user.setTotalPeriodPayed(0);
                            user.setBalance(0.0);
                            user.setBalanceUsed(0.0);
                            user.setPeriodeTertinggal(1);
                            user.setJoinDate(System.currentTimeMillis());
                            user.setActive(true);
                            user.setImagePath("");
                            user.setImageURL("");
                            userList.add(user);
                        }
//                    });
            // Close WorkBook
            workbook.close();
            return userList;
        } catch (IOException e) {
            throw new RuntimeException("FAIL! -> message = " + e.getMessage());
        }
    }

}
