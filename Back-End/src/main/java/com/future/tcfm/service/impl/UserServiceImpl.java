package com.future.tcfm.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.tcfm.model.Group;
import com.future.tcfm.model.JwtUserDetails;
import com.future.tcfm.model.User;
import com.future.tcfm.repository.GroupRepository;
import com.future.tcfm.repository.JwtUserDetailsRepository;
import com.future.tcfm.repository.UserRepository;
import com.future.tcfm.service.EmailService;
import com.future.tcfm.service.NotificationService;
import com.future.tcfm.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.activation.FileTypeMap;
import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.future.tcfm.config.SecurityConfig.getCurrentUser;
import static com.future.tcfm.service.impl.ExpenseServiceImpl.createPageRequest;
import static com.future.tcfm.service.impl.NotificationServiceImpl.*;

@Service
public class UserServiceImpl implements UserService {
    public static final String UPLOADED_FOLDER="../assets/";
    public static final String UPLOADED_URL = "http://localhost:8088/img/";

    @Autowired
    UserRepository userRepository;
    @Autowired
    GroupRepository groupRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    NotificationService notificationService;

    @Autowired
    EmailService emailService;

    @Autowired
    JwtUserDetailsRepository jwtUserDetailsRepository;

    @Autowired
    MongoTemplate mongoTemplate;
    private String notifMessage;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository=userRepository;
    }

    @Override
    public List<User> loadAll() {
        return userRepository.findAll();
    }

    @Override
    public ResponseEntity getUserById(String id) {
        User userExist = userRepository.findByIdUser(id);
        if(userExist==null) return new ResponseEntity<>("User not found!",HttpStatus.NOT_FOUND);
        return new ResponseEntity(userExist,HttpStatus.OK);
    }

    @Override
    public User getUser(String email) {
        return userRepository.findByEmail(email);
    }

    public static void saveUploadedFile(MultipartFile file,String name) throws IOException {
        if (!file.isEmpty()) {
            byte[] bytes = file.getBytes();
            Path path = Paths.get(UPLOADED_FOLDER +name);
            System.out.println(UPLOADED_FOLDER+name);
            Files.write(path, bytes);
        }
    }

    public static boolean checkImageFile(MultipartFile file) {
        if (file != null) {
            String fileName = file.getOriginalFilename();
            if (StringUtils.isEmpty(fileName)) {
                return false;
            }
            return file.getContentType().equals("image/png") || file.getContentType().equals("image/jpg") || file.getContentType().equals("image/jpeg") || file.getContentType().equals("image/bmp");
        }
        return false;
    }
    @Override
    public ResponseEntity getImage(String imageName) throws IOException {
        Path path = Paths.get(UPLOADED_FOLDER + imageName);
        File img = new File(String.valueOf(path));
        String mimetype = FileTypeMap.getDefaultFileTypeMap().getContentType(img);
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(mimetype))
                .body(Files.readAllBytes(img.toPath()));
    }

    @Override
    public Page<User> searchBy(String query,Boolean membersOnly, int page,int size) {
        System.out.println("Query Param : "+query);
        Pattern pattern = Pattern.compile("(.*)(:)(.*)");
        Matcher matcher = pattern.matcher(query);
        if(!matcher.find()){return null;}
        String key=matcher.group(1);
        String value=matcher.group(3);
        Criteria criteria;
        if(key.equalsIgnoreCase("group")) key="groupName";
        System.out.println("Key : "+key+"; Value : "+value);
        String myRole = getCurrentUser().getAuthorities().toString();
        String groupName = myRole.contains("SUPER_ADMIN") ? "" : getCurrentUser().getGroupName();
        Pageable pageable = createPageRequest(key,"asc",page,size);
        Sort sort = new Sort(Sort.Direction.ASC,"name");
        Query myQuery = new Query().with(pageable);
        criteria = Criteria.where(key).regex(value,"i").and("groupName").is(groupName).and("active").is(true);
        if(membersOnly){
            criteria = Criteria.where("groupName").is(getCurrentUser().getGroupName()).and("active").is(true);
        } else if(key.equalsIgnoreCase("period" ) && groupName.equalsIgnoreCase("")){ // for super admin
            criteria = Criteria.where("groupName").regex(groupName).and("active").is(true);
            sort = new Sort(Sort.Direction.DESC,"TotalPeriodPayed");
        } else if(key.equalsIgnoreCase("period" ) && !groupName.equalsIgnoreCase("")){ // for member
            criteria = Criteria.where("groupName").is(groupName).and("active").is(true);
            sort = new Sort(Sort.Direction.DESC,"TotalPeriodPayed");
        } else if(groupName.equalsIgnoreCase("")){
            criteria = Criteria.where(key).regex(value,"i").and("active").is(true);
        }
        myQuery.addCriteria(criteria).with(sort);
        List<User> paymentList =  mongoTemplate.find(myQuery,User.class,"user");
        return PageableExecutionUtils.getPage(
                paymentList,
                pageable,
                () -> mongoTemplate.count(myQuery, User.class));
    }

    /**
     *
     * @param newGroupAdmin
     * @param userExist // user data before request
     * @param user // new user data from request
     * @return
     */
    private void updateGroupAdmin(String newGroupAdmin,User userExist, User user) throws MessagingException {
        List<User> listUserGroup = new ArrayList<>();
        if(!newGroupAdmin.equalsIgnoreCase("")) { // set admin baru jika admin lama pindah role jadi member
            User newAdmin = userRepository.findByEmailAndActive(newGroupAdmin, true);
            if (newAdmin == null) {
//                return new ResponseEntity<>("Error 404:Email User Not found", HttpStatus.NOT_FOUND);
                throw new RuntimeException("Error, new admin(email) not fonud!");
            }
            newAdmin.setRole("GROUP_ADMIN");
            userRepository.save(newAdmin);

            Group oldGroup = groupRepository.findByNameAndActive(userExist.getGroupName(), true);
            oldGroup.setGroupAdmin(newGroupAdmin);
            groupRepository.save(oldGroup);
//            userExist.setGroupName(user.getGroupName().equalsIgnoreCase("") ? "GROUP_LESS" : user.getGroupName());
            notificationService.createNotification(newAdmin.getName() + " just been promoted to Group Admin!", null, newAdmin.getGroupName(), TYPE_GROUP);
            emailService.emailNotification("Congrats! you have been promoted to be Group Admin.",newAdmin.getEmail());
            notificationService.createNotification("Congrats! you have been promoted to be Group Admin.", newAdmin.getEmail(), null, TYPE_PERSONAL);
        }
    }
    /**
     * management user
     * @param id
     * @param user
     * @param newGroupAdmin //admin baru untuk grup lama jika yg pindaha dalah grup admin
     * @return
     */
    @Override
    public ResponseEntity manageUser(String id, User user, String newGroupAdmin) throws MessagingException {
        List<User> listUserGroup = new ArrayList<>();
        User userExist = userRepository.findByIdUser(id);
        Group groupExist = groupRepository.findByNameAndActive(user.getGroupName(),true); // new group
        Map<String,String> responseMap = new HashMap();
        if(userExist==null){
            return new ResponseEntity<>("Username not found!", HttpStatus.NOT_FOUND);
        }
        if(!userExist.getGroupName().equalsIgnoreCase(user.getGroupName())){
            if(userExist.getPeriodeTertinggal()>0){
                responseMap.put("message","Error : This user have not completed their payment ("+userExist.getPeriodeTertinggal().toString()+" periode(s) left).\nThis user have to complete his payment in order to switch group.");
                return new ResponseEntity<>(responseMap,HttpStatus.INTERNAL_SERVER_ERROR);
            }
            if(!newGroupAdmin.equalsIgnoreCase("")) {
                updateGroupAdmin(newGroupAdmin, userExist, user);
//                userExist.setGroupName(user.getGroupName().equalsIgnoreCase("") ? "GROUP_LESS" : user.getGroupName());
            }

            notifMessage = userExist.getEmail() + USER_LEFT_GROUP;
            notificationService.createNotification(notifMessage, userExist.getEmail(), userExist.getGroupName(), TYPE_GROUP);
            userExist.setGroupName(user.getGroupName().equalsIgnoreCase("") ? "GROUP_LESS" : user.getGroupName());

            userExist.setBalance(userExist.getGroupName().equalsIgnoreCase("GROUP_LESS") ? user.getBalance() :  0.0 );
            userExist.setBalanceUsed(userExist.getGroupName().equalsIgnoreCase("GROUP_LESS")? user.getBalanceUsed() : 0.0 );
            userExist.setPeriodeTertinggal(userExist.getGroupName().equalsIgnoreCase("GROUP_LESS") ?  user.getPeriodeTertinggal() : 1 );
            userExist.setTotalPeriodPayed(userExist.getGroupName().equalsIgnoreCase("GROUP_LESS") ?  user.getTotalPeriodPayed() : groupExist.getCurrentPeriod() - 1);

            userExist.setJoinDate(System.currentTimeMillis());
            //notification untuk group barunya
            notifMessage = userExist.getEmail() + USER_JOINED_GROUP;
            notificationService.createNotification(notifMessage, userExist.getEmail(), user.getGroupName(), TYPE_GROUP);
        } else{
            if(!newGroupAdmin.equalsIgnoreCase("")) {
                updateGroupAdmin(newGroupAdmin, userExist, user);
            }
            userExist.setBalance(user.getBalance());
            userExist.setBalanceUsed(user.getBalanceUsed());
            userExist.setTotalPeriodPayed(user.getTotalPeriodPayed());
            userExist.setPeriodeTertinggal(user.getPeriodeTertinggal());
        }
        if(!userExist.getGroupName().equalsIgnoreCase("GROUP_LESS") && groupExist != null){ //cek role jika pindah grup, timpa admin lama, bila roleny group admin juga
            if(user.getRole().equals("GROUP_ADMIN")){
//                if(!groupExist.getGroupAdmin().equalsIgnoreCase("")){
//                    return new ResponseEntity()
//                }
                User oldAdmin = userRepository.findByEmail(groupExist.getGroupAdmin());
                if(oldAdmin != null){
                    oldAdmin.setRole("MEMBER");
                    userRepository.save(oldAdmin);
                }
                groupExist.setGroupAdmin(user.getEmail());
                groupRepository.save(groupExist);
                notificationService.createNotification(userExist.getName() + " just been promoted to Group Admin!", null, userExist.getGroupName(), TYPE_GROUP);
                emailService.emailNotification("Congrats! you have been promoted to be Group Admin.",userExist.getEmail());
                notificationService.createNotification("Congrats! you have been promoted to be Group Admin.", userExist.getEmail(), null, TYPE_PERSONAL);
            }
        }

        userExist.setRole(user.getRole());
        userExist.setName(user.getName());
        userExist.setPhone(user.getPhone());
        if(!user.getPassword().equalsIgnoreCase("") && user.getPassword().length()>=5) {
            userExist.setPassword(passwordEncoder.encode(user.getPassword()));//ENCRYPTION PASSWORD
        }
        jwtUserDetailsRepository.deleteByEmail(userExist.getEmail());
        userRepository.save(userExist);
        return new ResponseEntity(userExist,HttpStatus.OK);
    }

    /**
     * function untuk user utk mengupdate profilenya
     * @param id
     * @param userJSONString
     * @param file
     * @return
     * @throws IOException
     */
    @Override
    public ResponseEntity updateUserV2(String id, String userJSONString, MultipartFile file) throws IOException {
        User user  = new ObjectMapper().readValue(userJSONString, User.class);
        User userExist = userRepository.findByIdUser(id);
        if(userExist==null){
            return new ResponseEntity("Username not found!", HttpStatus.NOT_FOUND);
        }
        if(checkImageFile(file)){
            try{
                if(userExist.getImagePath()!=null) {
                    Path deletePath = Paths.get(UPLOADED_FOLDER + userExist.getImagePath());
                    Files.delete(deletePath);
                }
                String fileName="user/"+userExist.getEmail()+"_"+file.getOriginalFilename();
                saveUploadedFile(file,fileName);
                userExist.setImagePath(fileName);
                userExist.setImageURL(UPLOADED_URL+fileName);
            }catch (IOException e){
                return new ResponseEntity<>("Some error occured. Failed to add image", HttpStatus.BAD_REQUEST);
            }
        }
        userExist.setName(user.getName());
        userExist.setPhone(user.getPhone());
        if(!user.getPassword().equalsIgnoreCase("") && user.getPassword().length()>=5) {
            userExist.setPassword(passwordEncoder.encode(user.getPassword()));//ENCRYPTION PASSWORD
        }
        userRepository.save(userExist);
        return new ResponseEntity(userExist,HttpStatus.OK);
    }


    @Override
    public ResponseEntity createUserV2(String userJSONString, MultipartFile file) throws IOException, MessagingException {
        User user  = new ObjectMapper().readValue(userJSONString, User.class);
        System.out.println(user);
        User userExist = userRepository.findByEmailAndActive(user.getEmail(),true);
        Group groupExist = groupRepository.findByNameAndActive(user.getGroupName(),true);
        if(userExist!=null){
            return new ResponseEntity("Username/password already existed!", HttpStatus.BAD_REQUEST);
        }
        if (groupExist == null){
            groupExist = new Group();
            groupExist.setCurrentPeriod(1);
            groupExist.setRegularPayment((double)0);
            user.setGroupName("GROUP_LESS");
//            return new ResponseEntity<>("Failed to save User!\nGroup doesn't exists!", HttpStatus.BAD_REQUEST);
        }
        if (user.getRole().equals("GROUP_ADMIN")){
            if(!groupExist.getGroupAdmin().equalsIgnoreCase(""))

                return new ResponseEntity<>("Failed to save User!\nGroup admin already exists!", HttpStatus.BAD_REQUEST);
            else{
                groupExist.setGroupAdmin(user.getEmail());
                groupRepository.save(groupExist);// save perubahan admin pada group
            }
        }

        if(checkImageFile(file)){
            try{
                String fileName="user/"+user.getEmail()+"_"+file.getOriginalFilename();
                saveUploadedFile(file,fileName);
                user.setImagePath(fileName);
                user.setImageURL(UPLOADED_URL+fileName);
            }catch (IOException e){
                return new ResponseEntity<>("Some error occured. Failed to add image", HttpStatus.BAD_REQUEST);
            }
        }
        if(user.getTotalPeriodPayed()==null){
            user.setTotalPeriodPayed(0);
        }
        user.setTotalPeriodPayed(groupExist.getCurrentPeriod()+user.getTotalPeriodPayed()-1);//-1 karena bulan sekarang
        user.setPeriodeTertinggal(groupExist.getCurrentPeriod()-user.getTotalPeriodPayed());
        user.setJoinDate(System.currentTimeMillis());
        if(!user.getPassword().equalsIgnoreCase("") && user.getPassword().length()>=5) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));//ENCRYPTION PASSWORD
        }else return new ResponseEntity(HttpStatus.BAD_REQUEST);
        user.setActive(true);
        user.setBalance((user.getTotalPeriodPayed()-groupExist.getCurrentPeriod()+1)*groupExist.getRegularPayment());
        user.setBalanceUsed((double)0);

        userRepository.save(user);
        notifMessage= user.getName()+USER_JOINED_GROUP;
        notificationService.createNotification(notifMessage,user.getEmail(),user.getGroupName(),TYPE_GROUP);
        emailService.emailNotification(notifMessage,user.getEmail());
        return new ResponseEntity<>("Succeed to create user!",HttpStatus.OK);
    }

    //
    @Override
    public ResponseEntity<?> deleteUser(String id) throws MessagingException {
        User userExist = userRepository.findByIdUser(id);
        if (userExist == null)
            return new ResponseEntity<>("Failed to delete User!\nUserId not found!", HttpStatus.BAD_REQUEST);
        Map responseMap = new HashMap();
        if(userExist.getPeriodeTertinggal()>0){
            responseMap.put("message","Error : This user have not completed their payment ("+userExist.getPeriodeTertinggal().toString()+" periode(s) left).\nThis user have to complete his payment before resignation.");
            return new ResponseEntity<>(responseMap,HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if(userExist.getBalance()<0){
            responseMap.put("message","Error : This user has balance < 0");
            return new ResponseEntity<>(responseMap,HttpStatus.INTERNAL_SERVER_ERROR);
        }
//        if(userExist.getRole().equalsIgnoreCase("GROUP_ADMIN") && !userExist.getGroupName().equalsIgnoreCase("GROUP_LESS")){
//            responseMap.put("message","Please set a new Group Admin before resign.");
//            return new ResponseEntity<>(responseMap,HttpStatus.INTERNAL_SERVER_ERROR);
//        }
        userExist.setActive(false);
        jwtUserDetailsRepository.deleteByEmail(userExist.getEmail());
        userRepository.save(userExist);
        emailService.userResign(userExist.getEmail());
        responseMap.put("message","User resigned succeed.");
        return new ResponseEntity<>(responseMap, HttpStatus.OK);
    }


}

