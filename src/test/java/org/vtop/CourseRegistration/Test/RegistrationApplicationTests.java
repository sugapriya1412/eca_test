/*package org.vtop.CourseRegistration.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.vtop.CourseRegistration.model.CourseAllocationModel;
import org.vtop.CourseRegistration.model.CourseCatalogModel;
import org.vtop.CourseRegistration.repository.RegistrationLogRepository;
import org.vtop.CourseRegistration.service.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RegistrationApplicationTests {
	
	@Autowired private RegistrationLogRepository registrationLogRepository;
	
	@Autowired private RegistrationLogService registrationLogService;
	
	@Autowired private CourseCatalogService courseCatalogService;
	
	@Autowired private CourseRegistrationCommonFunction courseRegCommonFn;
	
	@Autowired private CourseTypeComponentService courseTypeComponentService;
	
	@Autowired private CourseTypeMasterService courseTypeMasterService;
	
	@Autowired private CourseAllocationService courseAllocationService;
	
	@Autowired private EmployeeProfileService employeeProfileService;
	
	
	private static final String[] classType = { "BFS" };
	private static final String RegErrorMethod = "SS1201819REG";
	
	private static final String CAMPUSCODE = "VLR";	
	//private static final int BUTTONS_TO_SHOW = 5;
	//private static final int INITIAL_PAGE = 0;
	//private static final int INITIAL_PAGE_SIZE = 5;
	//private static final int[] PAGE_SIZES = { 5, 10, 15, 20 };
	
					
	@Test
	public void contextLoads()
	{
	
	}
	
	@Test
	public void AddorDropDateTimeCheck() throws ParseException
	{
		System.out.println("Date & Time Check Test:");
		
		SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy");
		Date startDate = format.parse("05-MAR-2019");
		Date endDate = format.parse("23-MAR-2019");
		String startTime = "10:00:00", endTime = "24:00:00";
		String registerNumber = "17BCS0041";
		Integer updateStatus = 1;
		
		int timeCheckFlag = 2;
		String timeCheckMessage = "NONE", presentDateTime = "", presentTime = "";
		Long startTimeVal = 0L, endTimeVal = 0L, presentTimeVal = 0L;
		
		Date presentDate = null;
		DateFormat format1 = new SimpleDateFormat("dd-MMM-yyyy");
		
		try
		{			
			startTimeVal = Long.parseLong(startTime.replace(":", ""));
			endTimeVal = Long.parseLong(endTime.replace(":", ""));
			
			presentDateTime = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(new Date());					
			String[] presentDateTimeArr = presentDateTime.split(" ");
			presentDate = format1.parse(presentDateTimeArr[0]);
			presentTime = presentDateTimeArr[1];
			presentTimeVal = Long.parseLong(presentTime.replace(":", ""));
			
			System.out.println("StartDate: "+ startDate +" | StartTime: "+ startTime 
										+" | Start Time Value: " + startTimeVal);
			System.out.println("EndDate: "+ endDate +" | EndTime: "+ endTime +" | EndTimeValue: " + endTimeVal);
			System.out.println("PresentDate: "+ presentDate +" | PresentTime: "+ presentTime
					 				+" | PresentTimeValue: " + presentTimeVal);
					
			if ((presentDate.compareTo(startDate) >= 0) && (presentDate.compareTo(endDate) <= 0))
			{				
				if ((startDate.compareTo(endDate) == 0) && (presentDate.compareTo(startDate) == 0) 
						&& (presentTimeVal < startTimeVal))
				{
					timeCheckMessage = "Registration starts at "+ startTime +" Hrs.";
				}
				else if ((startDate.compareTo(endDate) == 0) && (presentDate.compareTo(startDate) == 0) 
							&& (presentTimeVal >= startTimeVal) && (presentTimeVal <= endTimeVal))
				{
					timeCheckMessage = "Success.";
					timeCheckFlag = 1;
				}
				else if ((startDate.compareTo(endDate) == 0) && (presentDate.compareTo(startDate) == 0) 
							&& (presentTimeVal > endTimeVal))
				{
					timeCheckMessage = "Registration closed.";
				}
				else if ((startDate.compareTo(endDate) != 0) && (presentDate.compareTo(startDate) == 0) 
							&& (presentTimeVal < startTimeVal))
				{
					timeCheckMessage = "Registration starts at "+ startTime +" Hrs.";
				}
				else if ((startDate.compareTo(endDate) != 0) && (presentDate.compareTo(startDate) == 0) 
							&& (presentTimeVal >= startTimeVal))
				{
					timeCheckMessage = "Success.";
					timeCheckFlag = 1;
				}
				else if ((startDate.compareTo(endDate) != 0) && (presentDate.compareTo(startDate) > 0) 
							&& (presentDate.compareTo(endDate) < 0))
				{
					timeCheckMessage = "Success.";
					timeCheckFlag = 1;
				}
				else if ((startDate.compareTo(endDate) != 0) && (presentDate.compareTo(endDate) == 0) 
							&& (presentTimeVal <= endTimeVal))
				{
					timeCheckMessage = "Success.";
					timeCheckFlag = 1;
				}
				else
				{
					timeCheckMessage = "Registration closed.";
				}
			}
			else
			{
				if (presentDate.compareTo(endDate) > 0)
				{
					timeCheckMessage = "Registration closed.";
				}
				else
				{
					timeCheckMessage = "Registration will start on "+ new SimpleDateFormat("dd-MMM-yyyy").format(startDate) 
											+" at "+ startTime +" Hrs.";
				}
			}
			
			if ((timeCheckFlag == 1) && (updateStatus == 1))
			{
				registrationLogRepository.UpdateActiveTimeStamp(registerNumber);
			}
		}
		catch (Exception ex)
		{
			//System.out.println(ex);
		}

		System.out.println("timeCheckFlag - >"+ timeCheckFlag +"Msg - >"+ timeCheckMessage);
	}

	//checking the Active Time
	@Test
	public void ActivePresentDateTimeCheck()
	{
		System.out.println("Current Date Time Check & update Active Time Stamp Test:");
		
		String registerNo="17BCS0041";
		int activeStatus = 1, logStatus = 0;
		long timeDiff = 0;
		List<Object[]> regLogList = new ArrayList<Object[]>();
					
		regLogList = registrationLogRepository.findRegistrationLogTimeDifference(registerNo);
		if (!regLogList.isEmpty())
		{
			for (Object[] e :regLogList)
			{
				logStatus = Integer.parseInt(e[0].toString());
				if (e[1] != null)
				{
					timeDiff = Long.parseLong(e[1].toString());
				}
				break;
			}
			//System.out.println("logStatus: "+ logStatus +" | timeDiff: "+ timeDiff);
				
			if ((logStatus == 1) && (timeDiff <= 300))
			{
					activeStatus = 2;
			}
		}
					
		System.out.println( activeStatus);
	}
	
	//Testing the Register Course
	@Test
	public void testCourseRegistration()
	{		
		String courseId = "VL_MAT3004_00110";
		String IpAddress = "10.10.95.16";
		String semesterSubId = "VL2018198";
		String registerNumber = "15BBT0234";
		
		String urlPage = "", courseTypeDisplay = "", msg = null, message = null, 
					courseOption = "", genericCourseType = "", infoMsg = "";
		String courseCategory = "NONE";
		String[] regStatusArr = new String[50];
		
		Integer updateStatus = 1;
		int allowStatus = 2, regStatusFlag = 2, projectStatus = 2, regAllowFlag = 1, wlAllowFlag = 1, 
				audAllowFlag = 1, rgrAllowFlag=2, minAllowFlag = 2, honAllowFlag = 2, adlAllowFlag = 2, 
				RPEUEAllowFlag=2;
		int ethExistFlag = 2, epjExistFlag = 2, epjSlotFlag = 2;
				
		try
		{			
			if ((semesterSubId!=null))
			{				
				if (courseCatalogService.isExist(courseId))
				{						
					CourseCatalogModel courseCatalogModel = new CourseCatalogModel();
					
					String pSemesterSubId = semesterSubId;
					String[] pCourseSystem = {"CAL"};
					Integer pProgramGroupId = 15; 
					String pProgramGroupCode = "BTECH";
					Integer pProgramSpecId = 2;
					String pProgramSpecCode = "BBT"; 
					Float CurriculumVersion = (float) 2.0;
					String pOldRegisterNumber = null; 
					Integer maxCredit = 27;
					
					String registrationOption = "CAL";
					String subCourseOption = "";
					Integer StudyStartYear = 2015;
					Float curriculumVersion = (float) 2.0;
					Integer StudentGraduateYear = 2019;
					
					Integer PEUEAllowStatus = 1;
					String studentStudySystem = "CAL";
					String[] classGroupId = "ALL".split("/");
					String programGroupMode = "Regular";
					
					SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy");
					Date startDate = format.parse("05-MAR-2019");
					Date endDate = format.parse("23-MAR-2019");
					String startTime = "10:00:00", endTime = "24:00:00";
										
					String returnVal = courseRegCommonFn.AddorDropDateTimeCheck(startDate, endDate, startTime, endTime, 
											registerNumber, updateStatus, IpAddress);
					String[] statusMsg = returnVal.split("/");
					allowStatus = Integer.parseInt(statusMsg[0]);
					infoMsg = statusMsg[1];
					
					List<String> courseTypeArr = new ArrayList<String>();					
					@SuppressWarnings("unchecked")
					//List<String> registerNumberList = (List<String>) session.getAttribute("registerNumberList");
					
					CourseCatalogModel ccm = new CourseCatalogModel();
					ccm = courseCatalogService.getOne(courseId);
					genericCourseType = ccm.getGenericCourseType();	
					
					String subCourseType = "", subCourseDate = "";
					switch(allowStatus)
					{
						
						case 1:
							
							courseCatalogModel = courseCatalogService.getOne(courseId);					
							String courseCode = courseCatalogModel.getCode();
							String genericCourseTypeDisplay = courseTypeComponentService.getOne(
																courseCatalogModel.getGenericCourseType()).getDescription();
							
							//System.out.println(pCourseSystem +" | "+ pProgramGroupId +" | "+ pProgramGroupCode +" | "+
							//		pProgramSpecCode +" | "+ pSemesterSubId +" | "+ registerNumber +" | "+ 
							//		pOldRegisterNumber +" | "+ maxCredit +" | "+ courseId +" | "+ StudyStartYear+" | "+
							//		StudentGraduateYear +" | "+ studentStudySystem);
							
							regStatusArr = courseRegCommonFn.CheckRegistrationCondition(pCourseSystem, pProgramGroupId, 
												pProgramGroupCode, pProgramSpecCode, pSemesterSubId, registerNumber, 
												pOldRegisterNumber, maxCredit, courseId, StudyStartYear, StudentGraduateYear, 
												studentStudySystem, pProgramSpecId,CurriculumVersion,PEUEAllowStatus, 
												programGroupMode, classGroupId).split("/");
							regStatusFlag = Integer.parseInt(regStatusArr[0]);
							message = regStatusArr[1];							
							courseOption = regStatusArr[2];
							regAllowFlag = Integer.parseInt(regStatusArr[3]);
							wlAllowFlag = Integer.parseInt(regStatusArr[4]);
							audAllowFlag = Integer.parseInt(regStatusArr[8]);
							rgrAllowFlag= Integer.parseInt(regStatusArr[11]);
							minAllowFlag = Integer.parseInt(regStatusArr[13]);
							honAllowFlag = Integer.parseInt(regStatusArr[12]);
							courseCategory=regStatusArr[14];
							adlAllowFlag=Integer.parseInt(regStatusArr[15]);
							String authKeyVal = regStatusArr[16];
							RPEUEAllowFlag=Integer.parseInt(regStatusArr[17]);
							
							//session.setAttribute("authStatus", authKeyVal);
							//System.out.println("RgR_Allow_Flag"+ rgrAllowFlag);
							
							if(PEUEStatusFlag == 1)
							{
								switch(courseOption)
								{
									case "RR":
									case "RRCE":									
										if (!regStatusArr[6].equals("NONE"))
										{
											courseTypeArr = Arrays.asList(regStatusArr[6].split(","));
										}																	
										
										if (courseTypeArr.size() <= 0)
										{
											courseTypeArr = courseTypeMasterService.getCourseTypeComponent(genericCourseType);
										}
										break;
									
									default:
										courseTypeArr = courseTypeMasterService.getCourseTypeComponent(genericCourseType);
										break;
								}
								
								switch(courseOption)
								{
									case "RR":
									case "RRCE":
									case "GI":
									case "GICE":
									case "RGCE":
									case "RPCE":
									case "RWCE":
										subCourseOption = regStatusArr[7];
										subCourseType = regStatusArr[9];
										subCourseDate = regStatusArr[10];
										break;
									default:
										if (regStatusArr[7].equals("NONE"))
										{
											subCourseOption = "";
										}
										break;
								}
								
													
								for (String crstp: courseTypeArr) 
								{
									if (courseTypeDisplay.equals(""))
									{
										courseTypeDisplay = courseTypeComponentService.getOne(crstp).getDescription();
									}
									else
									{
										courseTypeDisplay = courseTypeDisplay +" / "+ 
																courseTypeComponentService.getOne(crstp).getDescription();
									}
									
									if (crstp.equals("ETH"))
									{
										ethExistFlag = 1;
									}
									else if (crstp.equals("EPJ"))
									{
										epjExistFlag = 1;
									}								 
							    }
								
								if ((courseTypeArr.size() == 2) && (genericCourseType.equals("ETLP")) && 
										(ethExistFlag == 1) && (epjExistFlag == 1))
								{
									epjSlotFlag = 1;
								}
								else if ((courseTypeArr.size() == 1) && (epjExistFlag == 1))
								{
									epjSlotFlag = 1;
								}							
								
								switch(regStatusFlag)
								{    
									case 1:								
										if (courseTypeArr.size() > 0) 
										{
											for (String crtp : courseTypeArr) 
											{	
												//System.out.println("Course Type: "+ crtp);
												switch(crtp)
												{
													case "EPJ":
														List<CourseAllocationModel> epj = courseAllocationService.getCourseAllocationCourseIdTypeList(semesterSubId, classGroupId, classType, courseId, "EPJ");
														//for (CourseAllocationModel e: epj)
														//{
														//	System.out.println("Class Id: "+ e.getClassId());
														//}
														//model.addAttribute("cam3", epj);
														break;
													case "ELA":
														List<CourseAllocationModel> ela = courseAllocationService.getCourseAllocationCourseIdTypeList(semesterSubId, classGroupId, classType, courseId, "ELA");
														//for (CourseAllocationModel e: ela)
														//{
														//	System.out.println("Class Id: "+ e.getClassId());
														//}
														//model.addAttribute("cam2", ela);
														break;												
													default:
														List<CourseAllocationModel> list1 = courseAllocationService.getCourseAllocationCourseIdTypeList(semesterSubId, classGroupId, classType, courseId, crtp);
														//for (CourseAllocationModel e: list1)
														//{
														//	System.out.println("Class Id: "+ e.getClassId());
														//}
														//model.addAttribute("cam", list1);
														break;
												}
												
												switch(crtp)
												{
													case "PJT":
														projectStatus = 1;
														break;
												}											
											}
										}						
										
										if (projectStatus == 1)
										{	
											List<Object[]> courseCostCentre = employeeProfileService.getFacultyCostCentre(CAMPUSCODE);							
											model.addAttribute("ProgramCode", session.getAttribute("ProgramGroupCode"));
											model.addAttribute("courseCostCentre", courseCostCentre);
											model.addAttribute("courseOption", courseOption);										
											urlPage = "mainpages/ProjectRegistration::section";
										}
										else
										{	
											urlPage = "mainpages/CourseRegistration::section";
										}
										
										model.addAttribute("shcssList", studentHistoryService.getStudentHistoryCS2(registerNumberList, 
													courseCode, studentStudySystem, pProgramSpecId, StudyStartYear, curriculumVersion, 
													semesterSubId, courseCategory, courseOption));
										model.addAttribute("minorList", additionalLearningCourseCatalogService.getMinorTitleByCourseCode(
												courseCode, studentStudySystem, pProgramGroupId, pProgramSpecId, minAllowFlag));
										model.addAttribute("honorList", additionalLearningCourseCatalogService.getHonourTitleByCourseCode(
												courseCode, pProgramSpecCode, studentStudySystem, pProgramGroupId, pProgramSpecId, 
												honAllowFlag));					
										model.addAttribute("regAllowFlag", regAllowFlag);
										model.addAttribute("wlAllowFlag", wlAllowFlag);
										model.addAttribute("epjSlotFlag", epjSlotFlag);
										model.addAttribute("rgrAllowFlag", rgrAllowFlag);
										model.addAttribute("minAllowFlag", minAllowFlag);
										model.addAttribute("honAllowFlag", honAllowFlag);
										model.addAttribute("RPEUEAllowFlag", RPEUEAllowFlag);
										model.addAttribute("courseCatalogModel", courseCatalogService.getOne(courseId));
										model.addAttribute("page", page);
										model.addAttribute("srhType", searchType);
										model.addAttribute("srhVal", searchVal);
										model.addAttribute("courseOption", courseOption);
										model.addAttribute("registrationOption", registrationOption);						
										model.addAttribute("subCourseOption", subCourseOption);
										model.addAttribute("audAllowFlag", audAllowFlag);
										model.addAttribute("adlAllowFlag", adlAllowFlag);
										model.addAttribute("tlcourseType", courseTypeArr);					
										model.addAttribute("courseTypeDisplay", courseTypeDisplay);
										model.addAttribute("genericCourseTypeDisplay", genericCourseTypeDisplay);
										model.addAttribute("ProgramGroupCode", (String) session.getAttribute("ProgramGroupCode"));
										model.addAttribute("subCourseType", subCourseType);
										model.addAttribute("subCourseDate", subCourseDate);
										
										message = "Success";
										break;  
										
									case 2:
										//model.addAttribute("infoMessage", message);									
										urlPage = processRegistrationOption(registrationOption, model, session, 5, page, searchType, 
														searchVal, subCourseOption, request);
									break;  
								}					
							break;						
						//}
						//else
						//{
						//	session.removeAttribute("registrationOption");
						//	model.addAttribute("studySystem", studentStudySystem);
						//	model.addAttribute("maxCredit", maxCredit);
						//	model.addAttribute("registrationMethod", (String)session.getAttribute("registrationMethod"));
						//	model.addAttribute("ProgramGroupCode", (String) session.getAttribute("ProgramGroupCode"));							
						//	model.addAttribute("showFlag", 0);
						//	urlPage = "mainpages/RegistrationOptionList::section";
						//}
						default:
							msg = infoMsg;
							//session.setAttribute("info", msg);
							//model.addAttribute("flag", 2);
							urlPage = "redirectpage";
							message = msg;
							//return urlPage;
					}
				}
			}
			else
			{
				//model.addAttribute("flag", 1);
				urlPage = "redirectpage";
				message = "Session Problem.";
				//return urlPage;
			}
		}		
		catch(Exception ex)
		{
			//model.addAttribute("flag", 1);
			registrationLogService.addErrorLog(ex.toString(), RegErrorMethod+"CourseRegistrationFormController", 
					"processCourseRegistration", registerNumber, IpAddress);
			registrationLogRepository.UpdateLogoutTimeStamp2(IpAddress,registerNumber);
			
			urlPage = "redirectpage";
			message = ex.toString();
			//return urlPage;
		}		
		
		System.out.println("Course Registration Test: Checking all the Conditions & then display the slots to register a course");
		System.out.println("Status: "+ message);
		//return urlPage;
	}
}
*/