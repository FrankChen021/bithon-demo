package org.bithon.demo.user.client.config;

import org.bithon.demo.user.client.task.LivenessDetectionJob;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * @author frankchen
 * @create 2018年7月20日 下午2:48:55
 */
@Configuration
public class QuartzConfig {

    public static class AutowiringSpringBeanJobFactory extends SpringBeanJobFactory
        implements ApplicationContextAware {

        private final transient AutowireCapableBeanFactory beanFactory;

        public AutowiringSpringBeanJobFactory(final ApplicationContext context) {
            beanFactory = context.getAutowireCapableBeanFactory();
        }

        @Override
        protected Object createJobInstance(final TriggerFiredBundle bundle) throws Exception {
            final Object job = super.createJobInstance(bundle);
            beanFactory.autowireBean(job);
            return job;
        }
    }


    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(ApplicationContext applicationContext) throws IOException {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setAutoStartup(true);
        factory.setJobFactory(new AutowiringSpringBeanJobFactory(applicationContext));
        factory.setQuartzProperties(quartzProperties());
        //factory.setDataSource(dataSource);
        factory.setSchedulerName("user-client");
        factory.setOverwriteExistingJobs(true);
        return factory;
    }

    @Bean
    public Scheduler scheduler(SchedulerFactoryBean factory) throws SchedulerException {
        String name = "ping";
        String cronExpression = "0/10 * * ? * * *";

        Trigger trigger = TriggerBuilder.newTrigger()
                                        .withIdentity("trigger." + name,
                                                      "trigger.group." + name)
                                        .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                                        .build();

        JobDetail job = JobBuilder.newJob(LivenessDetectionJob.class)
                                  .storeDurably(true)
                                  .requestRecovery(false)
                                  .withIdentity("job." + name,
                                                "job.group." + name)
                                  .build();

        Scheduler scheduler = factory.getScheduler();
        scheduler.scheduleJob(job, trigger);
        scheduler.start();
        return scheduler;
    }

    private Properties quartzProperties() {
        Properties prop = new Properties();
        prop.put("org.quartz.scheduler.instanceId", "AUTO");
        prop.put("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");
        prop.put("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        prop.put("org.quartz.threadPool.threadCount", "2");
        prop.put("org.quartz.threadPool.threadPriority", "5");
        return prop;
    }
}


