package com.future.tcfm.service.impl;

import com.future.tcfm.model.Group;
import com.future.tcfm.model.User;
import com.future.tcfm.repository.GroupRepository;
import com.future.tcfm.repository.UserRepository;
import com.future.tcfm.service.EmailService;
import com.future.tcfm.service.NotificationService;
import com.future.tcfm.service.SchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import java.time.*;
import java.time.format.TextStyle;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static com.future.tcfm.service.impl.NotificationServiceImpl.PAYMENT_LATE;
import static com.future.tcfm.service.impl.NotificationServiceImpl.TYPE_PERSONAL;

@Service
public class SchedulerServiceImpl implements SchedulerService {
    @Autowired
    EmailService emailService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    NotificationService notificationService;

    private ExecutorService sseMvcExecutor = Executors.newSingleThreadExecutor();

    //
    @Async
    @Scheduled(cron = "0 10 10 05 * ?") // setiap tanggal 10  disetiap bulan jam 10 : 05
    public void scheduler() throws MessagingException {
        List<User> listUser = userRepository.findAllByActive(true);
        Map<String, Group> groupMap = new HashMap<>();
        listUser.forEach(user -> {
            if (user.getGroupName().equalsIgnoreCase("") || user.getGroupName().equalsIgnoreCase("GROUP_LESS")) {
            } else
                groupMap.put(user.getGroupName(), groupRepository.findByName(user.getGroupName()));
        });

        groupMap.forEach((groupName, groupVal) -> {
            if (groupVal.getCurrentPeriod() == null) {
                groupVal.setCurrentPeriod(1);
            } else {
                groupVal.setCurrentPeriod(groupVal.getCurrentPeriod() + 1); //misalkan sudah berganti bulan, maka update period group
            }
            groupRepository.save(groupVal);
        });

        sseMvcExecutor.execute(() -> {//pisahThread
            int yearBefore = 0;
            int monthChecker = 0;
            int yearChecker = 0;
            int monthBefore = 0;
            int monthNow = 0;
            int yearNow = 0;

            Group group;

            String monthBeforeStr = "";//untuk mendapatkan value bulan yang belum dibayar user
            for (User user : listUser) {
                group = groupMap.get(user.getGroupName());
                yearNow = Instant.ofEpochMilli(group.getCreatedDate()).atZone(ZoneId.systemDefault()).toLocalDate().getYear();
                monthNow = Instant.ofEpochMilli(group.getCreatedDate()).atZone(ZoneId.systemDefault()).toLocalDate().getMonthValue()-1;
                monthNow += group.getCurrentPeriod();

/*                Exception in thread "pool-5-thread-1" java.lang.NullPointerException
                at com.future.tcfm.service.impl.SchedulerServiceImpl.lambda$scheduler$2(SchedulerServiceImpl.java:80)
                at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
                at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
                at java.lang.Thread.run(Thread.java:748)*/

                if (monthNow > 12) {
                    monthChecker = monthNow % 12;
                    yearChecker = (monthNow - monthChecker) / 12;
                    monthNow = monthChecker;
                    yearNow += yearChecker;
                    if (monthNow == 0) {
                        monthNow += 12;
                        yearNow -= 1;
                    }
                }

                yearBefore = yearNow;
                monthBefore = monthNow;
                String monthNowStr = Month.of(monthNow).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
                user.setPeriodeTertinggal(user.getPeriodeTertinggal() + 1);
                if (user.getPeriodeTertinggal() > 0) { //jika true berarti user belum membayar iuran
                    if (user.getPeriodeTertinggal() > 11) {

                        monthChecker = user.getPeriodeTertinggal() % 12;
                        yearChecker = (user.getPeriodeTertinggal() - monthChecker) / 12;
                        monthBefore -= monthChecker;
                        yearBefore -= yearChecker;
                    } else {
                        monthBefore -= user.getPeriodeTertinggal() - 1; //dikurang 1 karena untuk mendapaktan value bulan yang belum di bayar
                    }

                    if (monthBefore <= 0) {
                        monthBefore += 12;
                        yearBefore -= 1;
                    }
                    monthBeforeStr = Month.of(monthBefore).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
//                notificationService.createNotification("You haven't made any payment from "+ monthBeforeStr+" to "+monthNowStr, user.getEmail(),user.getGroupName(),TYPE_PERSONAL);
                    notificationService.createNotification("You have missed " + user.getPeriodeTertinggal() + "'s month payment", user.getEmail(), user.getGroupName(), TYPE_PERSONAL);
                    try {
                        if (user.getPeriodeTertinggal() == 1)
                            emailService.periodicMailSender(user.getEmail(), "THISMONTH", yearBefore, monthNowStr, yearNow);
                        else
                            emailService.periodicMailSender(user.getEmail(), monthBeforeStr, yearBefore, monthNowStr, yearNow);
                    } catch (MessagingException e) {
                        e.printStackTrace();
                    }
                } else {
                    notificationService.createNotification("Thank you for completing " + monthNowStr + "'s payment", user.getEmail(), user.getGroupName(), TYPE_PERSONAL);
                    try {
                        emailService.periodicMailSender(user.getEmail(), monthBeforeStr, yearBefore, monthNowStr, yearNow);
                    } catch (MessagingException e) {
                        e.printStackTrace();
                    }
                }
            }
            userRepository.saveAll(listUser);
        });
    }

    @Async
    @Scheduled(cron = "0 7 10 05 * ?") // setiap tanggal 7 disetiap bulan jam 10 : 05
    public void schedulerReminder() throws MessagingException {
        List<User> listUser = userRepository.findAllByActive(true);
        sseMvcExecutor.execute(() -> {//pisahThread
            for (User user : listUser) {
                try {
                    if (user.getGroupName().equalsIgnoreCase("") || user.getGroupName().equalsIgnoreCase("GROUP_LESS")) {
                    }
                    else {
                        emailService.periodicMailReminderSender(user.getEmail());
                    }
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Async
    @Scheduled(cron = "0 31 10 05 * ?") // setiap tanggal 31 disetiap bulan jam 10 : 05
    public void monthlyCashStatement() throws MessagingException {
        List<User> listUser = userRepository.findAllByActive(true);
        sseMvcExecutor.execute(() -> {//pisahThread
            for (User user : listUser) {
                try {
                    if (user.getGroupName().equalsIgnoreCase("") || user.getGroupName().equalsIgnoreCase("GROUP_LESS")) {
                    } else {
                        emailService.monthlyCashStatement(user.getEmail());
                    }
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
