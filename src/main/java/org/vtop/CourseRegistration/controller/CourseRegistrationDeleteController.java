package org.vtop.CourseRegistration.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.vtop.CourseRegistration.model.CourseRegistrationModel;
import org.vtop.CourseRegistration.model.CourseRegistrationWaitingModel;
import org.vtop.CourseRegistration.service.CourseEquivalanceRegService;
import org.vtop.CourseRegistration.service.CourseRegistrationCommonFunction;
import org.vtop.CourseRegistration.service.CourseRegistrationService;
import org.vtop.CourseRegistration.service.CourseRegistrationWaitingService;
import org.vtop.CourseRegistration.service.ProgrammeSpecializationCurriculumDetailService;
import org.vtop.CourseRegistration.service.ProjectRegistrationService;
import org.vtop.CourseRegistration.service.RegistrationLogService;


@Controller
public class CourseRegistrationDeleteController 
{
	@Autowired private CourseRegistrationService courseRegistrationService;
	@Autowired private CourseEquivalanceRegService courseEquivalanceRegService;
	@Autowired private ProjectRegistrationService projectRegistrationService;
	@Autowired private CourseRegistrationCommonFunction courseRegCommonFn;
	@Autowired private RegistrationLogService registrationLogService;
	@Autowired private ProgrammeSpecializationCurriculumDetailService programmeSpecializationCurriculumDetailService;
	@Autowired private CourseRegistrationWaitingService courseRegistrationWaitingService;
	
	//private static final Logger logger = LogManager.getLogger(CourseRegistrationDeleteController.class);
	private static final String RegErrorMethod = "WSECAREG-2122";
	
	
	@PostMapping("processDeleteCourseRegistration")
	public String processDeleteCourseRegistration(String courseId, Model model, HttpSession session, 
						HttpServletRequest request) 
	{
		String registerNumber = (String) session.getAttribute("RegisterNumber");
		String IpAddress = (String) session.getAttribute("IpAddress");
		Integer WaitingListStatus=(Integer) session.getAttribute("waitingListStatus");
		int pageAuthStatus = 2;
		String pageAuthKey = "";
		Integer allowStatus = 0;
		Integer updateStatus = 1;
		int delStatusFlag = 2, deleteAllowStatus = 0;
		String urlPage = "", msg = null, infoMsg = "", courseAuthStatus = "",deleteMessage="";
		List<CourseRegistrationModel> courseRegistrationModel = new ArrayList<CourseRegistrationModel>();
		List<Object[]> courseRegistrationModel2 = new ArrayList<Object[]>();
		List<Object[]> courseRegistrationWaitingModel2 = new ArrayList<Object[]>();
		
		pageAuthKey = (String) session.getAttribute("pageAuthKey");
		pageAuthStatus = courseRegCommonFn.validatePageAuthKey(pageAuthKey, registerNumber, 2);
		
		String[] regStatusArr = new String[5];
		
		try
		{	
			if ((registerNumber != null) && (pageAuthStatus == 1))
			{	
				session.setAttribute("pageAuthKey", courseRegCommonFn.generatePageAuthKey(registerNumber, 2));
				String semesterSubId = (String) session.getAttribute("SemesterSubId");
				String[] classGroupId = session.getAttribute("classGroupId").toString().split("/");
				int studyStartYear = (int) session.getAttribute("StudyStartYear");
				Integer programGroupId = (Integer) session.getAttribute("ProgramGroupId");
				String ProgramSpecCode = (String) session.getAttribute("ProgramSpecCode");
				Integer programSpecId = (Integer) session.getAttribute("ProgramSpecId");
				String programGroupCode = (String) session.getAttribute("ProgramGroupCode");
				String programGroupMode = (String) session.getAttribute("programGroupMode");
				Float CurriculumVersion = (Float) session.getAttribute("curriculumVersion");
				int otpStatus = (int) session.getAttribute("otpStatus");
				
				@SuppressWarnings("unchecked")
				List<String> compCourseList = (List<String>) session.getAttribute("compulsoryCourseList");			
				Date startDate = (Date) session.getAttribute("startDate");
				Date endDate = (Date) session.getAttribute("endDate");
				String startTime = (String) session.getAttribute("startTime");
				String endTime = (String) session.getAttribute("endTime");
				
				String returnVal = courseRegCommonFn.AddorDropDateTimeCheck(startDate, endDate, startTime, endTime, 
										registerNumber, updateStatus, IpAddress);
				String[] statusMsg = returnVal.split("/");
				allowStatus = Integer.parseInt(statusMsg[0]);
				infoMsg = statusMsg[1];
				
				switch(allowStatus)
				{
					case 1:				
						regStatusArr = courseRegCommonFn.checkRegistrationDeleteCondition(semesterSubId, registerNumber, 
											courseId, programGroupId, programGroupCode, programGroupMode, ProgramSpecCode, 
											programSpecId, studyStartYear, CurriculumVersion, compCourseList).split("\\|");
						delStatusFlag = Integer.parseInt(regStatusArr[0]);
						deleteMessage = regStatusArr[1];
						courseAuthStatus = regStatusArr[2];
						
						if (otpStatus == 1)
						{
							deleteAllowStatus = 1;
						}
						else if (otpStatus == 2)
						{
							courseAuthStatus = courseRegCommonFn.generateCourseAuthKey(registerNumber, courseId, 
													delStatusFlag, 2);
						}
						
						session.setAttribute("authStatus", courseAuthStatus);
						
						if (delStatusFlag == 1)
						{							
							courseRegistrationModel = courseRegistrationService.getByRegisterNumberCourseIdByClassGroupId(
															semesterSubId, registerNumber, courseId, classGroupId);
														
							model.addAttribute("courseId", courseId);
							model.addAttribute("courseRegistrationModel", courseRegistrationModel);
							model.addAttribute("msg", deleteMessage);
							model.addAttribute("WaitingListStatus", WaitingListStatus);
							model.addAttribute("tlDeleteAllowStatus", deleteAllowStatus);
							
							urlPage = "mainpages/DeleteConfirmation :: section";
						}
						else
						{
							courseRegistrationModel2 = courseRegistrationService.getByRegisterNumberAndClassGroup(
																semesterSubId, registerNumber, classGroupId);
							courseRegistrationWaitingModel2 = courseRegistrationWaitingService.getWaitingCourseByRegNoWithRankByClassGroupId(
																	semesterSubId, registerNumber, classGroupId);
	
							model.addAttribute("courseRegistrationModel", courseRegistrationModel2);
							model.addAttribute("courseRegistrationWaitingModel", courseRegistrationWaitingModel2);
							model.addAttribute("blockedCourse", courseRegistrationService.getBlockedCourseIdByRegisterNumberForDelete(
									semesterSubId, registerNumber));
							model.addAttribute("curriculumMapList", programmeSpecializationCurriculumDetailService.
									getCurriculumBySpecIdYearAndCCVersionAsMap(programSpecId, studyStartYear, 
											CurriculumVersion));
							model.addAttribute("showFlag", 0);
							model.addAttribute("WaitingListStatus", WaitingListStatus);
							model.addAttribute("infoMessage", deleteMessage);
	
							urlPage = "mainpages/DeleteCourse :: section";
						}
						break;
					
					default:						
						msg = infoMsg;						
						session.setAttribute("info", msg);
						model.addAttribute("flag", 2);
						urlPage = "redirectpage";
						return urlPage;						
				}
			}
			else
			{
				model.addAttribute("flag", 1);
				urlPage = "redirectpage";
				return urlPage;
			}
		}
		catch(Exception e)
		{
			model.addAttribute("flag", 1);
			registrationLogService.addErrorLog(e.toString(), RegErrorMethod+"CourseRegistrationDeleteController", 
					"processDeleteCourseRegistration", registerNumber, IpAddress);
			registrationLogService.UpdateLogoutTimeStamp2(IpAddress,registerNumber);
			urlPage = "redirectpage";
			return urlPage;
		}		
		return urlPage;
	}		

	@PostMapping("processDeleteCourseRegistrationOTP")
	public String processDeleteCourseRegistrationOTP(String courseId, Model model, HttpSession session, 
						HttpServletRequest request) 
	{
		String registerNumber = (String) session.getAttribute("RegisterNumber");
		String IpAddress = (String) session.getAttribute("IpAddress");
		Integer WaitingListStatus=(Integer) session.getAttribute("waitingListStatus");
		int pageAuthStatus = 2;
		String pageAuthKey = "";
		Integer allowStatus = 0;
		Integer updateStatus = 1;
		
		int deleteAllowStatus = 0, redirectFlag = 2, statusFlag = 2;
		String urlPage = "", msg = null, infoMsg = "", courseAuthStatus = "", deleteMessage = "";
		String[] validateStatusArr = new String[]{};
		List<CourseRegistrationModel> courseRegistrationModel = new ArrayList<CourseRegistrationModel>();
		List<Object[]> courseRegistrationModel2 = new ArrayList<Object[]>();
		List<Object[]> courseRegistrationWaitingModel2 = new ArrayList<Object[]>();
		
		pageAuthKey = (String) session.getAttribute("pageAuthKey");
		pageAuthStatus = courseRegCommonFn.validatePageAuthKey(pageAuthKey, registerNumber, 2);
		
		
		try
		{	
			if ((registerNumber != null) && (pageAuthStatus == 1))
			{	
				session.setAttribute("pageAuthKey", courseRegCommonFn.generatePageAuthKey(registerNumber, 2));
				String semesterSubId = (String) session.getAttribute("SemesterSubId");
				String[] classGroupId = session.getAttribute("classGroupId").toString().split("/");
				int studyStartYear = (int) session.getAttribute("StudyStartYear");
				Integer programSpecId = (Integer) session.getAttribute("ProgramSpecId");
				Float CurriculumVersion = (Float) session.getAttribute("curriculumVersion");
				String studentEMailId = (String) session.getAttribute("studentEMailId");
				int otpStatus = (int) session.getAttribute("otpStatus");
							
				Date startDate = (Date) session.getAttribute("startDate");
				Date endDate = (Date) session.getAttribute("endDate");
				String startTime = (String) session.getAttribute("startTime");
				String endTime = (String) session.getAttribute("endTime");
				
				String returnVal = courseRegCommonFn.AddorDropDateTimeCheck(startDate, endDate, startTime, endTime, 
										registerNumber, updateStatus, IpAddress);
				String[] statusMsg = returnVal.split("/");
				allowStatus = Integer.parseInt(statusMsg[0]);
				infoMsg = statusMsg[1];
				
				switch(allowStatus)
				{
					case 1:
						String authStatus = (String) session.getAttribute("authStatus");
						int authCheckStatus = courseRegCommonFn.validateCourseAuthKey(authStatus, registerNumber, 
													courseId, 1);
						
						if ((authCheckStatus == 1) && (otpStatus == 1))
						{							
							validateStatusArr = courseRegCommonFn.validateCourseAndSendOTP(semesterSubId, registerNumber, 
													courseId, studentEMailId, IpAddress, "DELETE").split("\\|");
							statusFlag = Integer.parseInt(validateStatusArr[0]);
							courseAuthStatus = validateStatusArr[1];
							deleteMessage = validateStatusArr[3];
							//System.out.println("statusFlag: "+ statusFlag +" | courseAuthStatus: "+ courseAuthStatus 
							//		+" | deleteMessage: "+ deleteMessage); 
							
							if (statusFlag == 1)
							{
								deleteAllowStatus = 2;
								redirectFlag = 1;
							}
						}
						else
						{
							deleteMessage = "Invalid course...!";
						}
						
				
						if (redirectFlag == 1)
						{
							courseRegistrationModel = courseRegistrationService.getByRegisterNumberCourseIdByClassGroupId(
															semesterSubId, registerNumber, courseId, classGroupId);
							session.setAttribute("authStatus", courseAuthStatus);
							
							model.addAttribute("courseId", courseId);
							model.addAttribute("courseRegistrationModel", courseRegistrationModel);
							model.addAttribute("msg", deleteMessage);
							model.addAttribute("WaitingListStatus", WaitingListStatus);
							model.addAttribute("tlDeleteAllowStatus", deleteAllowStatus);
							
							urlPage = "mainpages/DeleteConfirmation :: section";
						}
						else
						{
							courseRegistrationModel2 = courseRegistrationService.getByRegisterNumberAndClassGroup(
																semesterSubId, registerNumber, classGroupId);
							courseRegistrationWaitingModel2 = courseRegistrationWaitingService.getWaitingCourseByRegNoWithRankByClassGroupId(
																	semesterSubId, registerNumber, classGroupId);
	
							model.addAttribute("courseRegistrationModel", courseRegistrationModel2);
							model.addAttribute("courseRegistrationWaitingModel", courseRegistrationWaitingModel2);
							model.addAttribute("blockedCourse", courseRegistrationService.getBlockedCourseIdByRegisterNumberForDelete(
									semesterSubId, registerNumber));
							model.addAttribute("curriculumMapList", programmeSpecializationCurriculumDetailService.
									getCurriculumBySpecIdYearAndCCVersionAsMap(programSpecId, studyStartYear, 
											CurriculumVersion));
							model.addAttribute("showFlag", 0);
							model.addAttribute("WaitingListStatus", WaitingListStatus);
							model.addAttribute("infoMessage", deleteMessage);
	
							urlPage = "mainpages/DeleteCourse :: section";
						}
						break;
					
					default:						
						msg = infoMsg;						
						session.setAttribute("info", msg);
						model.addAttribute("flag", 2);
						urlPage = "redirectpage";
						return urlPage;						
				}
			}
			else
			{
				model.addAttribute("flag", 1);
				urlPage = "redirectpage";
				return urlPage;
			}
		}
		catch(Exception e)
		{
			model.addAttribute("flag", 1);
			registrationLogService.addErrorLog(e.toString(), RegErrorMethod+"CourseRegistrationDeleteController", 
					"processDeleteCourseRegistration", registerNumber, IpAddress);
			registrationLogService.UpdateLogoutTimeStamp2(IpAddress,registerNumber);
			urlPage = "redirectpage";
			return urlPage;
		}		
		return urlPage;
	}
	
	@PostMapping("processDeleteConfirmationCourseRegistration")
	public String processDeleteConfirmationCourseRegistration(String courseId, Model model, HttpSession session, 
						HttpServletRequest request) 
	{
		String registerNumber = (String) session.getAttribute("RegisterNumber");
		String IpAddress = (String) session.getAttribute("IpAddress");
		Integer WaitingListStatus = (Integer) session.getAttribute("waitingListStatus");
		
		int pageAuthStatus = 2, redirectFlag = 2, statusFlag = 2;
		String pageAuthKey = "", urlPage = "";
		String msg = null, message = null, infoMsg = "";
		
		Integer allowStatus = 2, updateStatus = 1;
		String oldCourseId = "", pDelStatus = "", mailOTP = "";
		String[] validateStatusArr = new String[]{};
		
		pageAuthKey = (String) session.getAttribute("pageAuthKey");
		pageAuthStatus = courseRegCommonFn.validatePageAuthKey(pageAuthKey, registerNumber, 2);
		
		try
		{	
			if ((registerNumber != null) && (pageAuthStatus == 1))
			{
				String semesterSubId = (String) session.getAttribute("SemesterSubId");			
				String[] classGroupId = session.getAttribute("classGroupId").toString().split("/");
				Integer programSpecId = (Integer) session.getAttribute("ProgramSpecId");
				int studyStartYear = (int) session.getAttribute("StudyStartYear");
				Float curriculumVersion = (Float) session.getAttribute("curriculumVersion");								
				Date startDate = (Date) session.getAttribute("startDate");
				Date endDate = (Date) session.getAttribute("endDate");
				String startTime = (String) session.getAttribute("startTime");
				String endTime = (String) session.getAttribute("endTime");
				int otpStatus = (int) session.getAttribute("otpStatus");
				
				String returnVal = courseRegCommonFn.AddorDropDateTimeCheck(startDate, endDate, startTime, endTime, 
										registerNumber, updateStatus, IpAddress);
				String[] statusMsg = returnVal.split("/");
				allowStatus = Integer.parseInt(statusMsg[0]);
				infoMsg = statusMsg[1];
				
				mailOTP = request.getParameter("mailOTP");
				if ((mailOTP != null) && (!mailOTP.equals("")))
				{
					mailOTP = mailOTP.trim();
				}
				else
				{
					mailOTP = "NONE";
				}
				
				List<Object[]> courseRegistrationModel = new ArrayList<Object[]>();
				List<Object[]> courseRegistrationWaitingModel = new ArrayList<Object[]>();
				
				String authStatus = (String) session.getAttribute("authStatus");
				int authCheckStatus = courseRegCommonFn.validateCourseAuthKey(authStatus, registerNumber, courseId, 2);
				
				switch(allowStatus)
				{
					case 1:
						if(authCheckStatus == 1)
						{
							if (otpStatus == 1)
							{
								validateStatusArr = courseRegCommonFn.validateCourseAndOTP(semesterSubId, registerNumber, 
														courseId, mailOTP, IpAddress, "DELETE").split("\\|");
								statusFlag = Integer.parseInt(validateStatusArr[0]);
								redirectFlag = Integer.parseInt(validateStatusArr[1]);
								//message = validateStatusArr[2];
								
								if(validateStatusArr[2].toString().equals("SUCCESS"))
								{
									msg = "Registered Course(s) Successfully Deleted.";
								}
								else
								{
									message = validateStatusArr[2].toString();
								}
							}
							else if (otpStatus == 2)
							{
								statusFlag = 1;
								redirectFlag = 2;
								//message = "SUCCESS";
							}
							//System.out.println("statusFlag: "+ statusFlag +" | redirectFlag: "+ redirectFlag 
							//		+" | message: "+ message); 
			
							oldCourseId = courseEquivalanceRegService.getEquivCourseByRegisterNumberAndCourseId(
												semesterSubId, registerNumber, courseId);
							if ((oldCourseId == null) || (oldCourseId.equals(null)))
							{
								oldCourseId = "";
							}
							
							if (statusFlag == 1)
							{
								pDelStatus = courseRegistrationService.courseRegistrationDelete(semesterSubId, 
													registerNumber, courseId, "DELETE", registerNumber, IpAddress, 
													"GEN", oldCourseId);
								if (pDelStatus.equals("SUCCESS"))
								{
									projectRegistrationService.deleteByRegisterNumberCourseId(semesterSubId, 
											registerNumber, courseId);
									msg = "Selected course successfully deleted.";
								}
								else if ((pDelStatus.equals("FAIL")) || (pDelStatus.substring(0, 5).equals("error")))
								{
									message = "Technical error.";
									registrationLogService.addErrorLog(pDelStatus.toString()+"<-CODE->"+courseId, RegErrorMethod+"CourseRegistrationDeleteController", 
											"processRegisterProjectCourseDELPROC", registerNumber, IpAddress);
									registrationLogService.UpdateLogoutTimeStamp2(IpAddress,registerNumber);
								}
								else
								{
									message = pDelStatus;
								}
							}
						}
						else
						{
							message = "Not a valid course to delete.";
						}	
												
						if (redirectFlag == 1)
						{							
							model.addAttribute("courseId", courseId);
							model.addAttribute("courseRegistrationModel", courseRegistrationService.getByRegisterNumberCourseIdByClassGroupId(
									semesterSubId, registerNumber, courseId, classGroupId));
							model.addAttribute("infoMessage", message);
							model.addAttribute("WaitingListStatus", WaitingListStatus);
							model.addAttribute("tlDeleteAllowStatus", 2);
							
							urlPage = "mainpages/DeleteConfirmation :: section";
						}
						else
						{
							courseRegistrationModel = courseRegistrationService.getByRegisterNumberAndClassGroup(
														semesterSubId, registerNumber, classGroupId);
							courseRegistrationWaitingModel = courseRegistrationWaitingService.getWaitingCourseByRegNoWithRankByClassGroupId(
																semesterSubId, registerNumber, classGroupId);
		
							model.addAttribute("courseRegistrationModel", courseRegistrationModel);
							model.addAttribute("courseRegistrationWaitingModel", courseRegistrationWaitingModel);
							model.addAttribute("blockedCourse", courseRegistrationService.getBlockedCourseIdByRegisterNumberForDelete(
									semesterSubId, registerNumber));
							model.addAttribute("curriculumMapList", programmeSpecializationCurriculumDetailService.
									getCurriculumBySpecIdYearAndCCVersionAsMap(programSpecId, studyStartYear, curriculumVersion));
							model.addAttribute("WaitingListStatus", WaitingListStatus);
							model.addAttribute("info", msg);
							model.addAttribute("infoMessage", message);
							
							urlPage = "mainpages/DeleteCourse::section";
						}
						break;
						
					default:						
						msg = infoMsg;						
						session.setAttribute("info", msg);
						model.addAttribute("flag", 2);
						urlPage = "redirectpage";
						return urlPage;
				}
			}
			else
			{
				model.addAttribute("flag", 1);
				urlPage = "redirectpage";
				return urlPage;
			}
		}
		catch(Exception e)
		{
			model.addAttribute("flag", 1);
			registrationLogService.addErrorLog(e.toString(), RegErrorMethod+"CourseRegistrationDeleteController", 
					"processDeleteConfirmationCourseRegistration", registerNumber, IpAddress);
			registrationLogService.UpdateLogoutTimeStamp2(IpAddress,registerNumber);
			urlPage = "redirectpage";
			return urlPage;
		}
		
		return urlPage;
	}		

			
	@PostMapping("processDeleteConfirmationCourseRegistrationRirect")
	public String processDeleteConfirmationCourseRegistrationRirect(Model model, HttpSession session, 
						HttpServletRequest request) 
	{		
		String registerNumber = (String) session.getAttribute("RegisterNumber");
		String IpAddress = (String) session.getAttribute("IpAddress");
		Integer WaitingListStatus=(Integer) session.getAttribute("waitingListStatus");
		
		String msg = null, infoMsg = "", urlPage = "";
		Integer allowStatus = 2;
		Integer updateStatus = 1;
		int pageAuthStatus = 2;
		String pageAuthKey = "";
		
		pageAuthKey = (String) session.getAttribute("pageAuthKey");
		pageAuthStatus = courseRegCommonFn.validatePageAuthKey(pageAuthKey, registerNumber, 2);
					
		try
		{	
			if ((registerNumber != null) && (pageAuthStatus == 1))
			{
				session.setAttribute("pageAuthKey", courseRegCommonFn.generatePageAuthKey(registerNumber, 2));
				String semesterSubId = (String) session.getAttribute("SemesterSubId");
				String[] classGroupId = session.getAttribute("classGroupId").toString().split("/");
				Integer programSpecId = (Integer) session.getAttribute("ProgramSpecId");
				int studyStartYear = (int) session.getAttribute("StudyStartYear");
				Float curriculumVersion = (Float) session.getAttribute("curriculumVersion");				
				Date startDate = (Date) session.getAttribute("startDate");
				Date endDate = (Date) session.getAttribute("endDate");
				String startTime = (String) session.getAttribute("startTime");
				String endTime = (String) session.getAttribute("endTime");
				
				String returnVal = courseRegCommonFn.AddorDropDateTimeCheck(startDate, endDate, startTime, endTime, 
										registerNumber, updateStatus, IpAddress);
				String[] statusMsg = returnVal.split("/");
				allowStatus = Integer.parseInt(statusMsg[0]);
				infoMsg = statusMsg[1];
				
				List<Object[]> courseRegistrationModel = new ArrayList<Object[]>();
				List<Object[]> courseRegistrationWaitingModel = new ArrayList<Object[]>();
				
				
				switch(allowStatus)
				{
					case 1:
						courseRegistrationModel = courseRegistrationService.getByRegisterNumberAndClassGroup(
														semesterSubId, registerNumber, classGroupId);
						courseRegistrationWaitingModel = courseRegistrationWaitingService.getWaitingCourseByRegNoWithRankByClassGroupId(
															semesterSubId, registerNumber, classGroupId);
						
						model.addAttribute("courseRegistrationModel", courseRegistrationModel);
						model.addAttribute("courseRegistrationWaitingModel", courseRegistrationWaitingModel);
						model.addAttribute("blockedCourse", courseRegistrationService.getBlockedCourseIdByRegisterNumberForDelete(
								semesterSubId, registerNumber));
						model.addAttribute("curriculumMapList", programmeSpecializationCurriculumDetailService.
								getCurriculumBySpecIdYearAndCCVersionAsMap(programSpecId, studyStartYear, curriculumVersion));
						model.addAttribute("WaitingListStatus", WaitingListStatus);
						urlPage = "mainpages/DeleteCourse::section";
						break;
						
					default:						
						msg = infoMsg;						
						session.setAttribute("info", msg);
						model.addAttribute("flag", 2);
						urlPage = "redirectpage";
						return urlPage;					
				}
			}
			else
			{
				model.addAttribute("flag", 1);
				urlPage = "redirectpage";
				return urlPage;
			}
		}
		catch(Exception e)
		{
			model.addAttribute("flag", 1);
			registrationLogService.addErrorLog(e.toString(), RegErrorMethod+"CourseRegistrationDeleteController", 
					"processDeleteConfirmationCourseRegistrationRirect", registerNumber, IpAddress);
			registrationLogService.UpdateLogoutTimeStamp2(IpAddress,registerNumber);
			urlPage = "redirectpage";
			return urlPage;
		}
		return urlPage;			
	}
	

	@PostMapping("deleteRegisteredCourse")
	public String deleteRegisteredCourse(Model model, HttpSession session, HttpServletRequest request) 
	{	
		String registerNumber = (String) session.getAttribute("RegisterNumber");
		String IpAddress = (String) session.getAttribute("IpAddress");
		Integer WaitingListStatus=(Integer) session.getAttribute("waitingListStatus");
		
		String msg = null, infoMsg = "", urlPage = "";
		Integer allowStatus = 2;
		Integer updateStatus = 1;
		int pageAuthStatus = 2;
		String pageAuthKey = "";
		pageAuthKey = (String) session.getAttribute("pageAuthKey");
		pageAuthStatus = courseRegCommonFn.validatePageAuthKey(pageAuthKey, registerNumber, 2);
		try
		{	
			if ((registerNumber != null) && (pageAuthStatus == 1))
			{
				session.setAttribute("pageAuthKey", courseRegCommonFn.generatePageAuthKey(registerNumber, 2));
				String[] classGroupId = session.getAttribute("classGroupId").toString().split("/");
				String semesterSubId = (String) session.getAttribute("SemesterSubId");
				Integer programSpecId = (Integer) session.getAttribute("ProgramSpecId");
				int studyStartYear = (int) session.getAttribute("StudyStartYear");
				Float curriculumVersion = (Float) session.getAttribute("curriculumVersion");
				Date startDate = (Date) session.getAttribute("startDate");
				Date endDate = (Date) session.getAttribute("endDate");
				String startTime = (String) session.getAttribute("startTime");
				String endTime = (String) session.getAttribute("endTime");
				
				String returnVal = courseRegCommonFn.AddorDropDateTimeCheck(startDate, endDate, startTime, endTime, 
										registerNumber, updateStatus, IpAddress);
				String[] statusMsg = returnVal.split("/");
				allowStatus = Integer.parseInt(statusMsg[0]);
				infoMsg = statusMsg[1];
				
				List<Object[]> courseRegistrationModel = new ArrayList<Object[]>();
				List<Object[]> courseRegistrationWaitingModel = new ArrayList<Object[]>();
				
				switch(allowStatus)
				{
					case 1:
						courseRegistrationModel = courseRegistrationService.getByRegisterNumberAndClassGroup(
														semesterSubId, registerNumber, classGroupId);
						courseRegistrationWaitingModel = courseRegistrationWaitingService.getWaitingCourseByRegNoWithRankByClassGroupId(
																semesterSubId, registerNumber, classGroupId);
						
						model.addAttribute("courseRegistrationModel", courseRegistrationModel);
						model.addAttribute("courseRegistrationWaitingModel", courseRegistrationWaitingModel);
						model.addAttribute("blockedCourse", courseRegistrationService.getBlockedCourseIdByRegisterNumberForDelete(
								semesterSubId, registerNumber));
						model.addAttribute("curriculumMapList", programmeSpecializationCurriculumDetailService.
								getCurriculumBySpecIdYearAndCCVersionAsMap(programSpecId, studyStartYear, curriculumVersion));
						model.addAttribute("showFlag", 0);
						model.addAttribute("WaitingListStatus", WaitingListStatus);
						urlPage = "mainpages/DeleteCourse::section";
						break;
					
					default:						
						msg = infoMsg;						
						session.setAttribute("info", msg);
						model.addAttribute("flag", 2);
						urlPage = "redirectpage";
						return urlPage;
				}
			}
			else
			{
				model.addAttribute("flag", 1);
				urlPage = "redirectpage";
				return urlPage;
			}
		}
		catch(Exception e)
		{
			model.addAttribute("flag", 1);
			registrationLogService.addErrorLog(e.toString(), RegErrorMethod+"CourseRegistrationDeleteController", 
					"deleteRegisteredCourse", registerNumber, IpAddress);
			registrationLogService.UpdateLogoutTimeStamp2(IpAddress,registerNumber);
			urlPage = "redirectpage";
			return urlPage;
		}
		return urlPage;
	}
	
	
	@PostMapping("processDeleteConfirmationCourseRegistrationWaiting")
	public String processDeleteConfirmationCourseRegistrationWaiting(String courseId, Model model, HttpSession session, 
						HttpServletRequest request) 
	{
		String registerNumber = (String) session.getAttribute("RegisterNumber");
		Integer WaitingListStatus=(Integer) session.getAttribute("waitingListStatus");
		String msg = null, infoMsg = "", urlPage = "";
		Integer allowStatus = 2, updateStatus = 1;
		String IpAddress=(String) session.getAttribute("IpAddress");
		int pageAuthStatus = 2;
		String pageAuthKey = "";
		
		pageAuthKey = (String) session.getAttribute("pageAuthKey");
		pageAuthStatus = courseRegCommonFn.validatePageAuthKey(pageAuthKey, registerNumber, 2);
		
		try
		{	
			if ((registerNumber != null) && (pageAuthStatus == 1))
			{
				session.setAttribute("pageAuthKey", courseRegCommonFn.generatePageAuthKey(registerNumber, 2));
				String semesterSubId = (String) session.getAttribute("SemesterSubId");
				String[] classGroupId = session.getAttribute("classGroupId").toString().split("/");
				Integer programSpecId = (Integer) session.getAttribute("ProgramSpecId");
				int studyStartYear = (int) session.getAttribute("StudyStartYear");
				Float curriculumVersion = (Float) session.getAttribute("curriculumVersion");
				String authStatus = (String) session.getAttribute("authStatus");
				Date startDate = (Date) session.getAttribute("startDate");
				Date endDate = (Date) session.getAttribute("endDate");
				String startTime = (String) session.getAttribute("startTime");
				String endTime = (String) session.getAttribute("endTime");
				
				String returnVal = courseRegCommonFn.AddorDropDateTimeCheck(startDate, endDate, startTime, endTime, 
										registerNumber, updateStatus, IpAddress);						
				String[] statusMsg = returnVal.split("/");
				allowStatus = Integer.parseInt(statusMsg[0]);
				infoMsg = statusMsg[1];
								
				int authCheckStatus = courseRegCommonFn.validateCourseAuthKey(authStatus, registerNumber, courseId, 1);
				List<Object[]> courseRegistrationModel = new ArrayList<Object[]>();
				List<Object[]> courseRegistrationWaitingModel = new ArrayList<Object[]>();
				
				switch(allowStatus)
				{
					case 1:
						if(authCheckStatus == 1)
						{
							synchronized (this)
							{	
								courseRegistrationWaitingService.addWaitingToWaitingMove(semesterSubId, registerNumber, 
										courseId, 0, IpAddress);		
								courseRegistrationWaitingService.deleteByRegisterNumberCourseId(semesterSubId, registerNumber, 
										courseId);	
								courseEquivalanceRegService.deleteByRegisterNumberCourseId(semesterSubId, registerNumber, 
										courseId);	
							}
							
							msg = "Waiting List Course(s) Successfully Deleted.";
						}
						else
						{
							msg = "Not a valid course to delete.";
						}
						
						courseRegistrationModel = courseRegistrationService.getByRegisterNumberAndClassGroup(
														semesterSubId, registerNumber, classGroupId);
						courseRegistrationWaitingModel = courseRegistrationWaitingService.getWaitingCourseByRegNoWithRankByClassGroupId(
																semesterSubId, registerNumber, classGroupId);
											
						model.addAttribute("courseRegistrationModel", courseRegistrationModel);
						model.addAttribute("courseRegistrationWaitingModel", courseRegistrationWaitingModel);
						model.addAttribute("blockedCourse", courseRegistrationService.getBlockedCourseIdByRegisterNumberForDelete(
																semesterSubId, registerNumber));
						model.addAttribute("curriculumMapList", programmeSpecializationCurriculumDetailService.
								getCurriculumBySpecIdYearAndCCVersionAsMap(programSpecId, studyStartYear, curriculumVersion));
						
						
						model.addAttribute("info", msg);
						model.addAttribute("WaitingListStatus", WaitingListStatus);
						urlPage = "mainpages/DeleteCourse::section";
						break;
					
					default:						
						msg = infoMsg;						
						session.setAttribute("info", msg);
						model.addAttribute("flag", 2);
						urlPage = "redirectpage";
						return urlPage;
				}				
			}
			else
			{
				model.addAttribute("flag", 1);
				urlPage = "redirectpage";
				return urlPage;
			}
		}
		catch(Exception e)
		{
			model.addAttribute("flag", 1);
			registrationLogService.addErrorLog(e.toString(), RegErrorMethod+"CourseRegistrationDeleteController", 
					"processDeleteConfirmationCourseRegistrationWaiting", registerNumber, IpAddress);
			registrationLogService.UpdateLogoutTimeStamp2(IpAddress,registerNumber);
			urlPage = "redirectpage";
			return urlPage;
		}
		return urlPage;				
	}
	
	@PostMapping("processDeleteCourseRegistrationWaiting")
	public String processDeleteCourseRegistrationWaiting(String courseId, Model model, HttpSession session, 
						HttpServletRequest request) 
	{
		String registerNumber = (String) session.getAttribute("RegisterNumber");
		Integer WaitingListStatus=(Integer) session.getAttribute("waitingListStatus");
		String msg = null, urlPage = "", infoMsg = "";
		Integer allowStatus = 0, updateStatus = 1;
		String IpAddress=(String) session.getAttribute("IpAddress");
		int pageAuthStatus = 2;
		int delStatusFlag=2;
		String pageAuthKey = "",deleteMessage="",courseAuthStatus="";
		pageAuthKey = (String) session.getAttribute("pageAuthKey");
		pageAuthStatus = courseRegCommonFn.validatePageAuthKey(pageAuthKey, registerNumber, 2);
		
		String[] regStatusArr = new String[5];
		
		try
		{	
			if ((registerNumber != null) && (pageAuthStatus == 1))
			{
				session.setAttribute("pageAuthKey", courseRegCommonFn.generatePageAuthKey(registerNumber, 2));
				String semesterSubId = (String) session.getAttribute("SemesterSubId");
				String[] classGroupId = session.getAttribute("classGroupId").toString().split("/");
				int studyStartYear = (int) session.getAttribute("StudyStartYear");
				Integer programGroupId = (Integer) session.getAttribute("ProgramGroupId");
				String ProgramSpecCode = (String) session.getAttribute("ProgramSpecCode");
				Integer programSpecId = (Integer) session.getAttribute("ProgramSpecId");
				String programGroupCode = (String) session.getAttribute("ProgramGroupCode");
				String programGroupMode = (String) session.getAttribute("programGroupMode");
				Float CurriculumVersion = (Float) session.getAttribute("curriculumVersion");
				
				@SuppressWarnings("unchecked")
				List<String> compCourseList = (List<String>) session.getAttribute("compulsoryCourseList");
				Date startDate = (Date) session.getAttribute("startDate");
				Date endDate = (Date) session.getAttribute("endDate");
				String startTime = (String) session.getAttribute("startTime");
				String endTime = (String) session.getAttribute("endTime");
				
				String returnVal = courseRegCommonFn.AddorDropDateTimeCheck(startDate, endDate, startTime, endTime, 
										registerNumber, updateStatus, IpAddress);
				String[] statusMsg = returnVal.split("/");
				allowStatus = Integer.parseInt(statusMsg[0]);
				infoMsg = statusMsg[1];
				
				List<CourseRegistrationWaitingModel> courseRegistrationWaitingModel = new ArrayList<CourseRegistrationWaitingModel>();
				List<Object[]> courseRegistrationModel2 = new ArrayList<Object[]>();
				List<Object[]> courseRegistrationWaitingModel2 = new ArrayList<Object[]>();	

				switch(allowStatus)
				{
					case 1:
						regStatusArr = courseRegCommonFn.checkRegistrationDeleteCondition(semesterSubId, registerNumber, 
											courseId, programGroupId, programGroupCode, programGroupMode, ProgramSpecCode, 
											programSpecId, studyStartYear, CurriculumVersion, compCourseList).split("\\|");
						delStatusFlag = Integer.parseInt(regStatusArr[0]);
						deleteMessage = regStatusArr[1];
						courseAuthStatus = regStatusArr[2];
						
						session.setAttribute("authStatus", courseAuthStatus);
						
						if (delStatusFlag == 1)
						{
							courseRegistrationWaitingModel = courseRegistrationWaitingService.getByRegisterNumberCourseIdByClassGroupId(
																	semesterSubId, registerNumber, courseId, classGroupId);
							
							model.addAttribute("courseId", courseId);
							model.addAttribute("courseRegistrationWaitingModel", courseRegistrationWaitingModel);
							model.addAttribute("msg", deleteMessage);
							model.addAttribute("WaitingListStatus", WaitingListStatus);
							
							urlPage = "mainpages/DeleteConfirmationWaiting::section";
						}
						else
						{
							courseRegistrationModel2 = courseRegistrationService.getByRegisterNumberAndClassGroup(
									semesterSubId, registerNumber, classGroupId);
							courseRegistrationWaitingModel2 = courseRegistrationWaitingService.getWaitingCourseByRegNoWithRankByClassGroupId(
									semesterSubId, registerNumber, classGroupId);
		
							model.addAttribute("courseRegistrationModel", courseRegistrationModel2);
							model.addAttribute("courseRegistrationWaitingModel", courseRegistrationWaitingModel2);
							model.addAttribute("blockedCourse", courseRegistrationService.getBlockedCourseIdByRegisterNumberForDelete(
									semesterSubId, registerNumber));
							model.addAttribute("curriculumMapList", programmeSpecializationCurriculumDetailService.
									getCurriculumBySpecIdYearAndCCVersionAsMap(programSpecId, studyStartYear, CurriculumVersion));
							model.addAttribute("showFlag", 0);
							model.addAttribute("infoMessage", deleteMessage);
							model.addAttribute("WaitingListStatus", WaitingListStatus);
							
							urlPage = "mainpages/DeleteCourse::section";
						}
						break;
						
					default:						
						msg = infoMsg;						
						session.setAttribute("info", msg);
						model.addAttribute("flag", 2);
						urlPage = "redirectpage";
						return urlPage;
				}
			}
			else
			{
				model.addAttribute("flag", 1);
				urlPage = "redirectpage";
				return urlPage;
			}			
		}
		catch(Exception e)
		{
			model.addAttribute("flag", 1);
			registrationLogService.addErrorLog(e.toString(), RegErrorMethod+"CourseRegistrationDeleteController", 
					"processDeleteCourseRegistrationWaiting", registerNumber, IpAddress);
			registrationLogService.UpdateLogoutTimeStamp2(IpAddress,registerNumber);
			urlPage = "redirectpage";
			return urlPage;
		}
		return urlPage;
	}
	
	@PostMapping("processDeleteConfirmationCourseRegistrationWaitingRirect")
	public String processDeleteConfirmationCourseRegistrationWaitingRirect(Model model, HttpSession session, 
						HttpServletRequest request) 
	{
		String registerNumber = (String) session.getAttribute("RegisterNumber");
		Integer WaitingListStatus=(Integer) session.getAttribute("waitingListStatus");
		String msg = null, urlPage = "", infoMsg = "";
		Integer allowStatus = 2;
		String IpAddress=(String) session.getAttribute("IpAddress");
		Integer updateStatus = 1;
		int pageAuthStatus = 2;
		String pageAuthKey = "";
		pageAuthKey = (String) session.getAttribute("pageAuthKey");
		pageAuthStatus = courseRegCommonFn.validatePageAuthKey(pageAuthKey, registerNumber, 2);
		
		try
		{	
			if ((registerNumber != null) && (pageAuthStatus == 1))
			{
				String semesterSubId = (String) session.getAttribute("SemesterSubId");				
				String[] classGroupId = session.getAttribute("classGroupId").toString().split("/");
				Integer programSpecId = (Integer) session.getAttribute("ProgramSpecId");
				int studyStartYear = (int) session.getAttribute("StudyStartYear");
				Float curriculumVersion = (Float) session.getAttribute("curriculumVersion");								
				Date startDate = (Date) session.getAttribute("startDate");
				Date endDate = (Date) session.getAttribute("endDate");
				String startTime = (String) session.getAttribute("startTime");
				String endTime = (String) session.getAttribute("endTime");
				
				String returnVal = courseRegCommonFn.AddorDropDateTimeCheck(startDate, endDate, startTime, endTime, 
										registerNumber, updateStatus, IpAddress);
				String[] statusMsg = returnVal.split("/");
				allowStatus = Integer.parseInt(statusMsg[0]);
				infoMsg = statusMsg[1];
				
				List<Object[]> courseRegistrationModel = new ArrayList<Object[]>();
				List<Object[]> courseRegistrationWaitingModel = new ArrayList<Object[]>();
				
				switch(allowStatus)
				{
					case 1:
						courseRegistrationModel = courseRegistrationService.getByRegisterNumberAndClassGroup(
														semesterSubId, registerNumber, classGroupId);
						courseRegistrationWaitingModel = courseRegistrationWaitingService.getWaitingCourseByRegNoWithRankByClassGroupId(
															semesterSubId, registerNumber, classGroupId);
						
						model.addAttribute("courseRegistrationModel", courseRegistrationModel);
						model.addAttribute("courseRegistrationWaitingModel", courseRegistrationWaitingModel);
						model.addAttribute("blockedCourse", courseRegistrationService.getBlockedCourseIdByRegisterNumberForDelete(
								semesterSubId, registerNumber));
						model.addAttribute("curriculumMapList", programmeSpecializationCurriculumDetailService.
								getCurriculumBySpecIdYearAndCCVersionAsMap(programSpecId, studyStartYear, curriculumVersion));
						model.addAttribute("WaitingListStatus", WaitingListStatus);
						
						urlPage = "mainpages/DeleteCourse::section";
						break;
						
					default:						
						msg = infoMsg;						
						session.setAttribute("info", msg);
						model.addAttribute("flag", 2);
						urlPage = "redirectpage";
						return urlPage;
				}
			}
			else
			{
				model.addAttribute("flag", 1);
				urlPage = "redirectpage";
				return urlPage;
			}
		}
		catch(Exception e)
		{
			model.addAttribute("flag", 1);
			registrationLogService.addErrorLog(e.toString(), RegErrorMethod+"CourseRegistrationDeleteController", 
					"processDeleteConfirmationCourseRegistrationWaitingRirect", registerNumber, IpAddress);
			registrationLogService.UpdateLogoutTimeStamp2(IpAddress,registerNumber);
			urlPage = "redirectpage";
			return urlPage;
		}
		return urlPage;
	}	
}
