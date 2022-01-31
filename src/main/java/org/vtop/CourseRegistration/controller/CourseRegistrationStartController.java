package org.vtop.CourseRegistration.controller;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.vtop.CourseRegistration.service.CourseRegistrationCommonFunction;
import org.vtop.CourseRegistration.service.RegistrationLogService;


@Controller
public class CourseRegistrationStartController
{
	@Autowired private CourseRegistrationCommonFunction courseRegCommonFn;
	@Autowired private RegistrationLogService registrationLogService;

	private static final String RegErrorMethod = "WS2122REG";
	
		
	@PostMapping("checkRegistration")
	public String checkRegistration(Model model,HttpSession session,HttpServletRequest request)
	{
		String urlPage = "",currentDateTimeStr="";
		Date currentDateTime = new Date();
		Integer updateStatus = 1;
		int pageAuthStatus = 2, allowStatus = 2;
		String pageAuthKey = "", info="";
		String registerNumber = "",IpAddress = "",startTime = "",endTime = "",returnVal = "",studentDetails = "";
		Date startDate,endDate;
		
				
		try
		{
			registerNumber = (String) session.getAttribute("RegisterNumber");
			studentDetails = (String) session.getAttribute("studentDetails");
			IpAddress = (String) session.getAttribute("IpAddress");
			pageAuthKey = (String) session.getAttribute("pageAuthKey");
			startDate = (Date) session.getAttribute("startDate");
			endDate = (Date) session.getAttribute("endDate");
			startTime = (String) session.getAttribute("startTime");
			endTime = (String) session.getAttribute("endTime");
			
			pageAuthStatus = courseRegCommonFn.validatePageAuthKey(pageAuthKey, registerNumber, 1);
			
			if ((registerNumber != null) && (pageAuthStatus == 1))
			{
				returnVal = courseRegCommonFn.AddorDropDateTimeCheck(startDate, endDate, startTime, endTime, 
								registerNumber, updateStatus, IpAddress);
				String[] statusMsg = returnVal.split("/");
				allowStatus = Integer.parseInt(statusMsg[0]);
				info = statusMsg[1];
				
				if (allowStatus == 1)
				{
					session.setAttribute("pageAuthKey", courseRegCommonFn.generatePageAuthKey(registerNumber, 2));
					urlPage = "mainpages/MainPage";
				}
				else
				{
					session.setAttribute("pageAuthKey", courseRegCommonFn.generatePageAuthKey(registerNumber, 1));
					urlPage = "RegistrationStart";
				}
			}
			else
			{
				model.addAttribute("flag", 1);
				registrationLogService.UpdateLogoutTimeStamp2(IpAddress,registerNumber);
				urlPage = "redirectpage";
			}
			
			currentDateTimeStr = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a").format(currentDateTime);
			model.addAttribute("CurrentDateTime", currentDateTimeStr);
			model.addAttribute("studentDetails", studentDetails);
			/**/
			model.addAttribute("startDate", new SimpleDateFormat("dd-MMM-yyyy").format(startDate));
			model.addAttribute("startTime", startTime);
			model.addAttribute("endTime", endTime);
			model.addAttribute("info", info);
		}
		catch(Exception ex)
		{
			model.addAttribute("flag", 1);
			registrationLogService.addErrorLog(ex.toString(), RegErrorMethod+"CourseRegistrationStartController", 
					"checkRegistration", registerNumber, IpAddress);
			registrationLogService.UpdateLogoutTimeStamp2(IpAddress,registerNumber);
			urlPage = "redirectpage";			
		}
		return urlPage;
	}
}
