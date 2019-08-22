package com.future.tcfm.service;

import javax.mail.MessagingException;

public interface SchedulerService {
   void scheduler() throws MessagingException;
   void schedulerReminder() throws MessagingException;
   void monthlyCashStatement() throws MessagingException;
}
