package org.vtop.CourseRegistration;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;


@WebListener("*")
public class AppSessionListener implements HttpSessionListener 
{
	//private static final Logger logger = LogManager.getLogger(AppSessionListener.class); 
	private static int sessionCount = 0;
	private static int maxSessions=0;
	   
	public static int getTotalActiveSession() 
	{
	   return sessionCount;
	}
	   
	public static int getMaxSessions() 
	{
		return maxSessions;
	}
	   
	   
	@Override
	public void sessionCreated(HttpSessionEvent sessionEvent) 
	{   
	   synchronized (this) 
	   {
		   sessionCount++;
		   if(sessionCount>maxSessions) 
		   {
			   maxSessions=sessionCount;
		   }
		}
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent sessionEvent) 
	{
	   synchronized (this) 
	   {
		   sessionCount--;
	   }
	}
}
