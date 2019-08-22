package com.future.tcfm.service.impl;

import com.future.tcfm.model.*;
import com.future.tcfm.model.ReqResModel.ExpenseRequest;
import com.future.tcfm.model.list.ExpenseIdContributed;
import com.future.tcfm.model.list.UserContributedList;
import com.future.tcfm.repository.ExpenseRepository;
import com.future.tcfm.repository.GroupRepository;
import com.future.tcfm.repository.UserRepository;
import com.future.tcfm.service.EmailService;
import com.future.tcfm.service.ExpenseService;
import com.future.tcfm.service.NotificationService;
;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.future.tcfm.config.SecurityConfig.getCurrentUser;
import static com.future.tcfm.service.impl.NotificationServiceImpl.*;

@Service
public class ExpenseServiceImpl implements ExpenseService {
    @Autowired
    ExpenseRepository expenseRepository;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    UserRepository userRepository;


    @Autowired
    EmailService emailService;

    @Autowired
    NotificationService notificationService;

    @Override
    public List<Expense> loadAll() {
        return expenseRepository.findAll();
    }

    private String notificationMessage;

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public List<Expense> expenseGroup(String groupName) {
        return expenseRepository.findByGroupNameLikeOrderByCreatedDateDesc(groupName);
    }


    @Override
    public ResponseEntity createExpense(Expense expense) throws MessagingException {
        /*Expense expenseExist = expenseRepository.findByTitle(expense.getTitle());
        if (expenseExist != null)
            return new ResponseEntity<>("Failed to request Expense!\nTitle already exists!", HttpStatus.BAD_REQUEST);*/
        User userExist = userRepository.findByEmail(expense.getRequester());
        Group groupExist = groupRepository.findByName(userExist.getGroupName());
        if (groupExist == null)
            return new ResponseEntity<>("404 :\nGroup not Found!", HttpStatus.NOT_FOUND);
        if(groupExist.getGroupBalance()<expense.getPrice()){
            return new ResponseEntity<>("{\"message\":\"Group balance is not enough\"}",HttpStatus.BAD_REQUEST);
        }
        expense.setCreatedDate(new Date().getTime());
        expense.setGroupName(userRepository.findByEmail(expense.getRequester()).getGroupName());
        expense.setCreatedDate(System.currentTimeMillis());
        expense.setGroupCurrentPeriod(groupExist.getCurrentPeriod());
        List<User> userContributed = userRepository.findByGroupNameAndActive(expense.getGroupName(),true);
        List<UserContributedList> userContributedLists=new ArrayList<>();
        for(User user:userContributed){
            UserContributedList u = new UserContributedList();
            u.setEmail(user.getEmail());
            u.setImageURL(user.getImageURL());
            userContributedLists.add(u);
        }
//        expense.setRequester(userRepository.findByEmail(expense.getRequester()).getName());
        expense.setUserContributed(userContributedLists);
        expense.setRequester(expense.getRequester());
        expenseRepository.save(expense);
        /*
        Bagian notifikasi...
         */
        String message = expense.getRequester() + EXPENSE_MESSAGE +"(" +expense.getTitle()+")";
        notificationService.createNotification(message,expense.getRequester(),expense.getGroupName(),TYPE_GROUP);
        executor.execute(() -> {
            try {
                for (User user : userContributed) {
                    emailService.emailNotification(message, user.getEmail());//pengiriman email untuk user yang berkontribusi pada expense
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return new ResponseEntity<>(expense, HttpStatus.OK);
    }

    @Override
    public ResponseEntity singleExpense(String id) {
        Expense expenseExist = expenseRepository.findByIdExpense(id);
        if (expenseExist!=null)
            return new ResponseEntity<>(expenseExist, HttpStatus.OK);
        else
            return new ResponseEntity<>("Expense Not Found!", HttpStatus.NOT_FOUND);
    }


    /**
     * Paging dibawah
     * @param userEmail
     * @return
     */
    @Override
    public Page<Expense> expensePageGroupByEmail(String userEmail,String filter, int page, int size) {
        User userSelected = userRepository.findByEmail(userEmail);
        String userGroup = userSelected.getGroupName();
        return expenseRepository.findByGroupNameOrderByCreatedDateDesc(userGroup,createPageRequest(filter,"desc",page,size));
}

    static Pageable createPageRequest(String filter,String direction,int page, int size) {
        if(direction.equals("asc")){
            return PageRequest.of(page,size,Sort.by(filter).ascending());
        }
        return PageRequest.of(page,size,Sort.by(filter).descending());
    }

    public void updateExpenseContributed(Expense expenseExist,List<User> listUser){
        double balanceUsed = expenseExist.getPrice()/listUser.size();
        List<ExpenseIdContributed> expenseIdContributeds;
        for (User user : listUser) {//add the expense to all user that contributed to this expense
            expenseIdContributeds = user.getExpenseIdContributed();
            if(expenseIdContributeds==null) {
                expenseIdContributeds = new ArrayList<>();
            }
            ExpenseIdContributed e = new ExpenseIdContributed();
            e.setIdExpense(expenseExist.getIdExpense());
            e.setUsedBalance(balanceUsed);
            expenseIdContributeds.add(e);
            if(user.getBalance()==null){
                user.setBalance((0.0));
            }
            user.setBalanceUsed(user.getBalance()+balanceUsed);
            user.setBalance(user.getBalance()-balanceUsed); //mengurangi balance user dengan pembagian pengeluaran
            user.setExpenseIdContributed(expenseIdContributeds);
            userRepository.save(user);
        }
    }



    //ini api di pakai untuk admin utk reject / approve request expense dari user group
    @Override
    public ResponseEntity managementExpense(ExpenseRequest expenseRequest) throws MessagingException {
        /* Untuk membuat list adminGroup, requester dan seluruh super admin*/
        List<User> sendTo = new ArrayList<>();
        List<User> superAdmin = userRepository.findByRoleAndActive("SUPER_ADMIN",true);
        User userTarget = new User();
        User adminTarget = new User();
        User requesterTarget = new User();
        Expense expenseExist = expenseRepository.findByIdExpense(expenseRequest.getId());
        if (expenseExist==null)
            return new ResponseEntity<>("Expense not found", HttpStatus.OK);
        if(!expenseExist.getGroupName().equals(getCurrentUser().getGroupName())){
            return new ResponseEntity<>("403 You are not the group admin of this group",HttpStatus.UNAUTHORIZED);
        }
        Group group = groupRepository.findByName(expenseExist.getGroupName());
        adminTarget.setEmail(group.getGroupAdmin());
        if(expenseRequest.getStatus()) {
            if(expenseExist.getStatus()!=null){
                return new ResponseEntity<>("Expense already approved!",HttpStatus.BAD_REQUEST);
            }
            if(expenseExist.getPrice()>group.getGroupBalance()){
                return new ResponseEntity<>("Cannot accept expense, not enough group balance!",HttpStatus.BAD_REQUEST);
            }
            expenseExist.setStatus(true);
            group.setGroupBalance(group.getGroupBalance()-expenseExist.getPrice());
            //notif...
            //+= jgn timpah
            List<User> listUser = userRepository.findByGroupNameAndActive(group.getName(),true);
            group.setBalanceUsed(group.getBalanceUsed()+expenseExist.getPrice());
            updateExpenseContributed(expenseExist,listUser);//update the user field with transactional
            groupRepository.save(group);
            notificationMessage = expenseExist.getRequester() + EXPENSE_APPROVED_MESSAGE +"(" +expenseExist.getTitle()+")";
        }
        else if(!expenseRequest.getStatus()) {
            expenseExist.setStatus(false);

            //notif...
            notificationMessage = expenseExist.getRequester() + EXPENSE_REJECTED_MESSAGE +"(" +expenseExist.getTitle()+")";
        }
        notificationService.createNotification(notificationMessage,expenseExist.getRequester(),expenseExist.getGroupName(),TYPE_GROUP);

        /* Untuk membuat list adminGroup, requester dan seluruh super admin*/
        for (User user:superAdmin){
            userTarget.setEmail(user.getEmail());
            sendTo.add(userTarget);
        }

        requesterTarget.setEmail(expenseExist.getRequester());
        sendTo.add(requesterTarget);
        sendTo.add(adminTarget);

//        executor.execute(() -> {
            try {
                for (User user : sendTo) {
                    emailService.emailNotification(notificationMessage, user.getEmail());//pengiriman email untuk user yang berkontribusi
                }
            }catch (Exception e){e.printStackTrace();}
//        });
        expenseExist.setLastModifiedAt(System.currentTimeMillis());
        expenseExist.setApprovedOrRejectedBy(getCurrentUser().getEmail());
        expenseRepository.save(expenseExist);
        return new ResponseEntity<>("Expense Updated", HttpStatus.OK);
    }

    @Override
    public Page<Expense> searchBy(String query, int page, int size) throws ParseException {
        String groupName = getCurrentUser().getGroupName();
        System.out.println("Query Param : "+query);
        Pattern pattern = Pattern.compile("(.*)(:)(.*)");
        Matcher matcher = pattern.matcher(query);
        if(!matcher.find()){return null;}
        String key=matcher.group(1);
        String value=matcher.group(3);
        System.out.println("Key : "+key+"; Value : "+value);
        if(key.equalsIgnoreCase("title")){
            return expenseRepository.findByGroupNameContainsAndTitleContainsIgnoreCaseOrderByCreatedDateDesc(groupName,value,createPageRequest("createdDate","desc",page,size));
        } else if(key.equalsIgnoreCase("status")){
            Boolean status = value.equalsIgnoreCase("accepted");
            status = value.equalsIgnoreCase("") || value.equalsIgnoreCase("waiting") ? null : status;
            return expenseRepository.findByGroupNameContainsAndStatusOrderByCreatedDateDesc(groupName,status,createPageRequest("createdDate","desc",page,size));
        } else if(key.equalsIgnoreCase("price <" )){
            Double dValue = value.equalsIgnoreCase("")? Double.MAX_VALUE : Double.parseDouble(value);
            return expenseRepository.findByGroupNameContainsAndPriceLessThanOrderByPriceDesc(groupName,dValue,createPageRequest("createdDate","desc",page,size));
        } else if(key.equalsIgnoreCase("price >" )) {
            Double dValue = value.equalsIgnoreCase("")? 0 :Double.parseDouble(value);
            return expenseRepository.findByGroupNameContainsAndPriceGreaterThanOrderByPriceDesc(groupName,dValue, createPageRequest("createdDate", "desc", page, size));
        } else if(key.equalsIgnoreCase("date before")){
            SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy");
            long timeStamp= System.currentTimeMillis();
            try {
                timeStamp = formatter.parse(value).getTime();
            }catch (Exception e){e.printStackTrace();}
            return expenseRepository.findByGroupNameContainsAndCreatedDateLessThanEqualOrderByStatus(groupName,timeStamp,createPageRequest("createdDate","desc",page,size));
        } else if(key.equalsIgnoreCase("date after")){
            SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy");
            long timeStamp= 0;
            try {
                timeStamp = formatter.parse(value).getTime();
            }catch (Exception e){e.printStackTrace();}
            return expenseRepository.findByGroupNameContainsAndCreatedDateGreaterThanEqualOrderByStatus(groupName,timeStamp,createPageRequest("createdDate","desc",page,size));
        }
        return null;
    }

    @Override
    public Expense getLastExpense() {
        return expenseRepository.findTop1ByGroupNameAndStatusOrderByCreatedDateDesc(getCurrentUser().getGroupName(),true);
    }

/*
    @Override
    public ResponseEntity management(ExpenseRequest request) {
        Expense expense = expenseRepository.findByIdExpense(request.getId());
        User user = userRepository.findByEmail(request.getEmail());
        Group groupDtl = groupRepository.findByName(expense.getGroupName());


        if(expense==null)
            return new ResponseEntity<>("Expense Not Found!",HttpStatus.NOT_FOUND);
        if (!expense.getApprovedDate().equals(0L) || !expense.getRejectedDate().equals(0L))
            return new ResponseEntity<>("Expense Already Approved / Rejected!", HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>("Expense Approved",HttpStatus.OK);
    }
*/

}


