package com.future.tcfm.service.impl;

import com.future.tcfm.model.Expense;
import com.future.tcfm.model.Group;
import com.future.tcfm.model.ReqResModel.EmailRequest;
import com.future.tcfm.model.User;
import com.future.tcfm.model.list.ExpenseContributedDetails;
import com.future.tcfm.model.list.ExpenseIdContributed;
import com.future.tcfm.repository.ExpenseRepository;
import com.future.tcfm.repository.GroupRepository;
import com.future.tcfm.repository.UserRepository;
import com.future.tcfm.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class EmailServiceImpl implements EmailService {

    private static final String PATH = "../assets/";

    @Autowired
    UserRepository userRepository;

    @Autowired
    ExpenseRepository expenseRepository;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    public JavaMailSender emailSender;

    ExecutorService executor = Executors.newSingleThreadExecutor();
//    email max 500 mail/day

    @Async
    public void periodicMailSender( String email, String monthBeforeStr,int yearBefore, String monthNowStr, int yearNow) throws MessagingException {

        User user  = userRepository.findByEmail(email);
        String name = user.getName();
        String groupName = user.getGroupName();

        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setTo("mhabibofficial2@gmail.com");
        helper.setSubject("Team Cash Flow Management: Monthly Reminder Regular Payment");

        if (monthNowStr.equals(monthBeforeStr)||monthBeforeStr.equals("")) {
            helper.setText("<html><body>" +
                    "<img src=\"https://ecp.yusercontent.com/mail?url=https%3A%2F%2Fattachment.freshdesk.com%2Finline%2Fattachment%3Ftoken%3DeyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpZCI6MzUwMTYyOTE1ODgsImRvbWFpbiI6ImJsaWJsaWNhcmUuZnJlc2hkZXNrLmNvbSIsImFjY291bnRfaWQiOjc4OTM5M30.cHSBN2d9_8FZrmY3y6-n5b5FY3RUzJ-4JV6SD_EWXfc&t=1563855732&ymreqid=f2fe503c-78f1-5207-1c52-e00005011400&sig=kAn2UYZJzmVcvzCbWALl_g--~C\" alt=\"www.blibli.com\" width=\"700\" height=\"100\" style=\"border:0px;\">" +
                    "<tr><td style=\"padding:15px;\"><p>Halo "+name+"<br><br>Kamu telah membayar iuran untuk bulan "+monthNowStr+" "+yearNow+"<br><br>Semoga hari anda menyenangkan. Terima Kasih.<br><br><br><br>Salam hangat,<br>Admin Team "+groupName+" - Blibli.com</p></td></tr></body></html>",true);
            System.out.println("-------------------------------------------------------------------------");
            System.out.println(name+" "+"Kamu telah membayar iuran untuk bulan "+monthNowStr+" "+yearNow);
            System.out.println("-------------------------------------------------------------------------");
        }
        else if (monthBeforeStr.equalsIgnoreCase("THISMONTH")) {
            helper.setText("<html><body>" +
                    "<img src=\"https://ecp.yusercontent.com/mail?url=https%3A%2F%2Fattachment.freshdesk.com%2Finline%2Fattachment%3Ftoken%3DeyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpZCI6MzUwMTYyOTE1ODgsImRvbWFpbiI6ImJsaWJsaWNhcmUuZnJlc2hkZXNrLmNvbSIsImFjY291bnRfaWQiOjc4OTM5M30.cHSBN2d9_8FZrmY3y6-n5b5FY3RUzJ-4JV6SD_EWXfc&t=1563855732&ymreqid=f2fe503c-78f1-5207-1c52-e00005011400&sig=kAn2UYZJzmVcvzCbWALl_g--~C\" alt=\"www.blibli.com\" width=\"700\" height=\"100\" style=\"border:0px;\">" +
                    "<tr><td style=\"padding:15px;\"><p>Halo "+name+"<br><br>Kamu belum membayar iuran untuk bulan "+monthNowStr+" "+yearNow+"<br>Segera lakukan pembayaran anda.<br><br>Semoga hari anda menyenangkan. Terima Kasih.<br><br><br><br>Salam hangat,<br>Admin Team "+groupName+" - Blibli.com</p></td></tr></body></html>",true);
            System.out.println("-------------------------------------------------------------------------");
            System.out.println(name+" "+"Kamu belum membayar iuran untuk bulan "+monthNowStr+" "+yearNow);
            System.out.println("-------------------------------------------------------------------------");
        }
        else {
            helper.setText("<html><body>" +
                    "<img src=\"https://ecp.yusercontent.com/mail?url=https%3A%2F%2Fattachment.freshdesk.com%2Finline%2Fattachment%3Ftoken%3DeyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpZCI6MzUwMTYyOTE1ODgsImRvbWFpbiI6ImJsaWJsaWNhcmUuZnJlc2hkZXNrLmNvbSIsImFjY291bnRfaWQiOjc4OTM5M30.cHSBN2d9_8FZrmY3y6-n5b5FY3RUzJ-4JV6SD_EWXfc&t=1563855732&ymreqid=f2fe503c-78f1-5207-1c52-e00005011400&sig=kAn2UYZJzmVcvzCbWALl_g--~C\" alt=\"www.blibli.com\" width=\"700\" height=\"100\" style=\"border:0px;\">" +
                    "<tr><td style=\"padding:15px;\"><p>Halo "+name+"<br><br>Kamu belum membayar iuran untuk bulan "+monthBeforeStr+" "+yearBefore+" - "+monthNowStr+" "+yearNow+"<br>Segera lakukan pembayaran anda.<br><br>Semoga hari anda menyenangkan. Terima Kasih.<br><br><br><br>Salam hangat,<br>Admin Team "+groupName+" - Blibli.com</p></td></tr></body></html>",true);
            System.out.println("-------------------------------------------------------------------------");
            System.out.println(name+" "+"Kamu belum membayar iuran untuk bulan "+monthBeforeStr+" "+yearBefore+" - "+monthNowStr+" "+yearNow);
            System.out.println("-------------------------------------------------------------------------");
        }
        this.emailSender.send(message);
    }
    @Async
    public void periodicMailReminderSender( String email) throws MessagingException {
        User user  = userRepository.findByEmail(email);
        String name = user.getName();
        String groupName = user.getGroupName();
        String monthNowStr=Month.of(LocalDate.now().getMonthValue()).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        int yearNow=LocalDate.now().getYear();

        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setTo("mhabibofficial2@gmail.com");
        helper.setSubject("Team Cash Flow Management: Monthly Reminder Regular Payment");

        helper.setText("<html><body>" +
                "<img src=\"https://ecp.yusercontent.com/mail?url=https%3A%2F%2Fattachment.freshdesk.com%2Finline%2Fattachment%3Ftoken%3DeyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpZCI6MzUwMTYyOTE1ODgsImRvbWFpbiI6ImJsaWJsaWNhcmUuZnJlc2hkZXNrLmNvbSIsImFjY291bnRfaWQiOjc4OTM5M30.cHSBN2d9_8FZrmY3y6-n5b5FY3RUzJ-4JV6SD_EWXfc&t=1563855732&ymreqid=f2fe503c-78f1-5207-1c52-e00005011400&sig=kAn2UYZJzmVcvzCbWALl_g--~C\" alt=\"www.blibli.com\" width=\"700\" height=\"100\" style=\"border:0px;\">" +
                "<tr><td style=\"padding:15px;\"><p>Halo "+name+"<br><br>Iuran bulanan group kamu akan dijalankan ke periode berikutnya, pada tanggal 10 "+monthNowStr+" "+yearNow+"<br>Pastikan anda telah membayar iuran bulanan anda.<br><br>Semoga hari anda menyenangkan. Terima Kasih.<br><br><br><br>Salam hangat,<br>Admin Team "+groupName+" - Blibli.com</p></td></tr></body></html>",true);

        this.emailSender.send(message);
    }

    @Async
    @Override
    public void emailNotification(String messages, String email) throws MessagingException {
        User user  = userRepository.findByEmail(email);
        String name = user.getName();

        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setTo("mhabibofficial2@gmail.com");
        helper.setSubject("Team Cash Flow Management: Notification");

        helper.setText("<html><body>" +
                "<img src=\"https://ecp.yusercontent.com/mail?url=https%3A%2F%2Fattachment.freshdesk.com%2Finline%2Fattachment%3Ftoken%3DeyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpZCI6MzUwMTYyOTE1ODgsImRvbWFpbiI6ImJsaWJsaWNhcmUuZnJlc2hkZXNrLmNvbSIsImFjY291bnRfaWQiOjc4OTM5M30.cHSBN2d9_8FZrmY3y6-n5b5FY3RUzJ-4JV6SD_EWXfc&t=1563855732&ymreqid=f2fe503c-78f1-5207-1c52-e00005011400&sig=kAn2UYZJzmVcvzCbWALl_g--~C\" alt=\"www.blibli.com\" width=\"700\" height=\"100\" style=\"border:0px;\">" +
                "<tr><td style=\"padding:15px;\"><p>Halo "+name+"<br><br>"+messages+"<br><br>Semoga hari anda menyenangkan. Terima Kasih.<br><br><br><br>Salam hangat,<br>Admin Team - Blibli.com</p></td></tr></body></html>",true);
        System.out.println("-------------------------------------------------------------------------");
        System.out.println("Halo "+name+"<br><br>"+messages);
        System.out.println("-------------------------------------------------------------------------");
        this.emailSender.send(message);
    }

    @Async
    @Override
    public void monthlyCashStatement(String email) throws MessagingException {
        String balanceUsed;
        User user  = userRepository.findByEmail(email);
        String name = user.getName();
        String groupName = user.getGroupName();
        String expenseListStr="";
        List<ExpenseContributedDetails> listExpense = new ArrayList<>();
        Group group= groupRepository.findByName(user.getGroupName());
        List<Expense> expenseIdContributed = expenseRepository.findByGroupNameLikeAndGroupCurrentPeriodAndStatus(groupName,group.getCurrentPeriod(),true);
        List<ExpenseIdContributed> expenseIdContributedUser = user.getExpenseIdContributed();
        if(expenseIdContributed!=null && expenseIdContributed.size()!=0){
            for(Expense expense: expenseIdContributed){
                NumberFormat n = NumberFormat.getCurrencyInstance();
                balanceUsed = n.format(expenseIdContributedUser.get(0).getUsedBalance());
                ExpenseContributedDetails expenseContributedDetails = new ExpenseContributedDetails();
                expenseContributedDetails.setTitle(expense.getTitle());
                expenseContributedDetails.setDetail(expense.getDetail());
                expenseContributedDetails.setPrice(balanceUsed);
                listExpense.add(expenseContributedDetails);
                expenseListStr+=(expenseContributedDetails.toString());
            }
        }
        else{
            expenseListStr="\"Ooopss!!! Group anda belum ada kontribusi :(\"";
        }

        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setTo("mhabibofficial2@yahoo.com");
        helper.setSubject("Team Cash Flow Management: Resignation");

        helper.setText("<html><body>" +
                "<img src=\"https://ecp.yusercontent.com/mail?url=https%3A%2F%2Fattachment.freshdesk.com%2Finline%2Fattachment%3Ftoken%3DeyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpZCI6MzUwMTYyOTE1ODgsImRvbWFpbiI6ImJsaWJsaWNhcmUuZnJlc2hkZXNrLmNvbSIsImFjY291bnRfaWQiOjc4OTM5M30.cHSBN2d9_8FZrmY3y6-n5b5FY3RUzJ-4JV6SD_EWXfc&t=1563855732&ymreqid=f2fe503c-78f1-5207-1c52-e00005011400&sig=kAn2UYZJzmVcvzCbWALl_g--~C\" alt=\"www.blibli.com\" width=\"700\" height=\"100\" style=\"border:0px;\">" +
                "<tr><td style=\"padding:15px;\"><p>Halo "+name+"<br><br>Berikut ini merupakan list expense group kamu bulan ini<br><br>"+expenseListStr+"<br><br>Semoga hari anda menyenangkan. Terima Kasih.<br><br><br><br>Salam hangat,<br>Admin Team "+groupName+" - Blibli.com</p></td></tr></body></html>",true);
        System.out.println("\n--------------------------------------------------------");
        System.out.println(name+" \n"+expenseListStr);
        System.out.println("--------------------------------------------------------\n");
        this.emailSender.send(message);
    }

    @Async
    @Override
    public ResponseEntity userResign(String email) throws MessagingException {
        User user  = userRepository.findByEmail(email);

        if(user.getPeriodeTertinggal()>0){
            return new ResponseEntity<>("Kamu belum bisa resign, Pembayaran iuran kamu belum lunas !", HttpStatus.BAD_REQUEST);
        }
        else{
            String userBalance;
            String balanceUsed;
            String name = user.getName();
            String groupName = user.getGroupName();
            String expenseListStr="";
            List<ExpenseContributedDetails> listExpense = new ArrayList<>();

            List<ExpenseIdContributed> expenseIdContributed = user.getExpenseIdContributed();
            if(expenseIdContributed!=null){
                for(ExpenseIdContributed expense: expenseIdContributed){
                    NumberFormat n = NumberFormat.getCurrencyInstance();
                    balanceUsed = n.format(expenseIdContributed.get(0).getUsedBalance());

                    Expense e = expenseRepository.findByIdExpense(expense.getIdExpense()) ;
                    ExpenseContributedDetails expenseContributedDetails = new ExpenseContributedDetails();
                    expenseContributedDetails.setTitle(e.getTitle());
                    expenseContributedDetails.setDetail(e.getDetail());
                    expenseContributedDetails.setPrice(balanceUsed);
                    listExpense.add(expenseContributedDetails);
                    expenseListStr+=(expenseContributedDetails.toString());
                }
            }
            else{
                expenseListStr="\"Ooopss!!! anda belum ada kontribusi dalam group ini :(\"";
            }
            NumberFormat n = NumberFormat.getCurrencyInstance();
            userBalance = n.format(user.getBalance());
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setTo("mhabibofficial2@gmail.com");
            helper.setSubject("Team Cash Flow Management: Resignation");

            helper.setText("<html><body>" +
                    "<img src=\"https://ecp.yusercontent.com/mail?url=https%3A%2F%2Fattachment.freshdesk.com%2Finline%2Fattachment%3Ftoken%3DeyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpZCI6MzUwMTYyOTE1ODgsImRvbWFpbiI6ImJsaWJsaWNhcmUuZnJlc2hkZXNrLmNvbSIsImFjY291bnRfaWQiOjc4OTM5M30.cHSBN2d9_8FZrmY3y6-n5b5FY3RUzJ-4JV6SD_EWXfc&t=1563855732&ymreqid=f2fe503c-78f1-5207-1c52-e00005011400&sig=kAn2UYZJzmVcvzCbWALl_g--~C\" alt=\"www.blibli.com\" width=\"700\" height=\"100\" style=\"border:0px;\">" +
                    "<tr><td style=\"padding:15px;\"><p>Halo "+name+"<br><br>Kamu Baru Saja Meninggalkan Group "+groupName+"<br><br>Berikut ini merupakan list penggunaan dana kamu<br><br>"+expenseListStr+"<br>Kamu telah membayar iuran group sebanyak "+user.getTotalPeriodPayed()+"x <br><br>Jumlah dana yang akan dikembalikan kepadamu ialah senilai : "+userBalance+"<br>Harap Hubungi Admin Group Untuk Prosedur Pengambilan Uang Kembali.<br><br>Semoga hari anda menyenangkan. Terima Kasih.<br><br><br><br>Salam hangat,<br>Admin Team "+groupName+" - Blibli.com</p></td></tr></body></html>",true);
            this.emailSender.send(message);
            System.out.println("\n--------------------------------------------------------");
            System.out.println(name+" \n"+expenseListStr+"\n Dana yang dikembalikan "+userBalance);
            System.out.println("--------------------------------------------------------\n");
        }
        return new ResponseEntity<>("Some error occured.", HttpStatus.BAD_REQUEST);
    }
}