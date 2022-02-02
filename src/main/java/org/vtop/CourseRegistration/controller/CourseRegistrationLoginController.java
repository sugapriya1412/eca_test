package org.vtop.CourseRegistration.controller;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.vtop.CourseRegistration.service.CompulsoryCourseConditionDetailService;
import org.vtop.CourseRegistration.service.CourseRegistrationCommonFunction;
import org.vtop.CourseRegistration.service.ProgrammeSpecializationCurriculumCreditService;
import org.vtop.CourseRegistration.service.ProgrammeSpecializationCurriculumDetailService;
import org.vtop.CourseRegistration.service.RegistrationLogService;
import org.vtop.CourseRegistration.service.SemesterMasterService;
import org.vtop.CourseRegistration.service.StudentHistoryService;


@Controller
public class CourseRegistrationLoginController 
{	
	@Autowired private CourseRegistrationCommonFunction courseRegCommonFn;
	@Autowired private StudentHistoryService studentHistoryService;
	@Autowired private ProgrammeSpecializationCurriculumCreditService programmeSpecializationCurriculumCreditService;
	@Autowired private RegistrationLogService registrationLogService;
	@Autowired private CompulsoryCourseConditionDetailService compulsoryCourseConditionDetailService;
	@Autowired private SemesterMasterService semesterMasterService;
	@Autowired private ProgrammeSpecializationCurriculumDetailService programmeSpecializationCurriculumDetailService;
		
	//private static final String CAMPUSCODE = "VLR";
	private static final String RegErrorMethod = "WS2122REG";
	
	
	@PostMapping("processStudentLogin")
	public String processStudentLogin(String userName, String password, String captchaString, 
						Model model, HttpSession session, HttpServletRequest request, 
						HttpServletResponse response) throws ServletException, IOException, 
						ParseException 
	{	
		int testStatus = 2; //Login with Password & Captcha-> 1: Enable/ 2: Disable
		int regSlotCheckStatus = 2; //If Permitted Schedule-> 1: Date & Time / 2: Only Date
		int regTimeCheckStatus = 1; //Time-> 1: Open Hours/ 2: Permitted Schedule
		int historyCallStatus = 1; //Student History-> 1: Procedure/ 2: Table
		int cgpaStatus = 1; //Student CGPA & Credit Detail-> 1: Dynamic/ 2: Static
		
		int PEUEAllowStatus = 1; //PE & UE Category Allow Status-> 1: Enable/ 2: Disable
		int approvalStatus = 1; //Registration Status Approval-> 1: Enable/ 2: Disable
		int waitingListStatus = 2; //Waiting List Allow Status-> 1: Enable/ 2: Disable
		int OptionNAStatus = 1; //Option Not Allowed Status-> 1: Enable/ 2: Disable
		int compulsoryCourseStatus = 2; //Compulsory Course Allow Status-> 1: Enable/ 2: Disable
		int otpStatus = 2; //OTP Send Status-> 1: Enable/ 2: Disable
		
		int maxCredit = 27, minCredit = 16, studyStartYear = 0, studentSemester = 0;	
		int studentGraduateYear = 0, academicYear = 0, academicGraduateYear = 0, cclTotalCredit = 0;				
		int lockStatus = 2, validateLogin = 2, activeStatus = 2, allowStatus = 2;
		int checkFlag = 2, checkFlag2 = 2, checkFlag3 = 2, checkFlag4 = 2, checkFlag5 = 2, checkFlag6 = 2;
		int checkFlag7 = 2, checkFlag8 = 2, checkFlag9 = 2, checkFlag10 = 2, checkFlag11 = 2;
		Integer updateStatus = 0, exemptionStatus = 0, regularFlag = 2,reRegFlag = 2,schStatus = 0;
		Integer programDuration = 0, groupId = 0, specId = 0, feeId = 0, graduationStatus = 0, costCenterId = 0;
		float cclVersion = 0;
				
		String registerNo = "", urlPage = "", semesterSubId = "", msg = "", sessioncaptchaString = "";
		String ipAddress = request.getRemoteAddr();		
		String registrationMethod = "GEN", classGroupId = "", studentCgpaData = "0|0|0";
		String programGroupCode = "", programGroupMode= "", costCentreCode = "", dbPassWord = "";	
		String eduStatus = "", studentName = "", specCode ="", specDesc = "", studentStudySystem = "", 
					studentHistoryStatus = "", studentCategory = "NONE", eduStatusExpn = "";
		String courseEligible = "", CGPAEligible = "", oldRegNo = "", studEMailId = "";
		String currentDateTimeStr = "", returnVal = "", checkCourseSystem = "";
				
		SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy");		
		Date startDate = format.parse("01-FEB-2022");
		Date endDate = format.parse("04-FEB-2022");
		String startTime = "10:00:00", endTime = "23:59:59", allowStartTime = "10:00:00";
		
		String[] statusMsg = new String[]{};
		String[] egbProgram = {};
		String[] courseSystem = new String[2];
		String[] registerNumberArray = new String[2];
		List<Integer> egbProgramInt = new ArrayList<Integer>();
		List<String> registerNumberList = new ArrayList<String>();
				
		Integer semesterId = 0,  wlCount = 0, regCount = 0;
		Integer regCredit = 0, wlCredit= 0;
		Date currentDateTime = new Date();		
		
		List<Object[]> lcObjList = new ArrayList<Object[]>();
		List<String> compulsoryCourseList = new ArrayList<String>();
						
		//Assigning IP address 
		if (request != null) {
			ipAddress = request.getHeader("X-FORWARDED-FOR");
            if (ipAddress == null || "".equals(ipAddress)) {
            	  ipAddress = request.getRemoteAddr();
            }
        }
		//System.out.println("userName: "+ userName +" | password: "+ password 
		//			+" | captchaString: "+ captchaString +" | ipAddress: "+ ipAddress);
		
		try
		{						
			//For getting captcha from session attribute					
			sessioncaptchaString = (String) session.getAttribute("CAPTCHA");
			//System.out.println("sessioncaptchaString: "+ sessioncaptchaString);
			
			//Checking the Registration Date/Time Duration based on Slot or General
			if (regTimeCheckStatus == 2)
			{
				msg = courseRegCommonFn.registrationSessionDateTimeCheck(startDate);
				if (msg.equals("SUCCESS"))
				{
					checkFlag = 1;
				}
			}
			else
			{
				returnVal = courseRegCommonFn.AddorDropDateTimeCheck(startDate, endDate, allowStartTime, endTime, 
								userName, updateStatus, ipAddress);
				statusMsg = returnVal.split("/");
				allowStatus = Integer.parseInt(statusMsg[0]);
				msg = statusMsg[1];
				//System.out.println("allowStatus: "+ allowStatus +" | msg: "+ msg);
				if (allowStatus == 1)
				{
					checkFlag = 1;
					msg = "";
				}
				else
				{
					msg = msg.replace(allowStartTime, startTime);
				}
			}
												
			//Validate the Input of Register No., Password and Captcha.
			if (checkFlag == 1)
			{
				if ((userName == null) || (userName.equals("")) || (password == null) || (password.equals(""))) 
				{
					msg = "Enter Username or Password.";
				}
				else if ((captchaString == null) || (captchaString.equals("")) || (captchaString.length() != 6))
				{
					msg = "Enter Captcha.";
				}
				else if ((sessioncaptchaString == null) || (sessioncaptchaString.equals("")))
				{
					courseRegCommonFn.callCaptcha(request,response,session,model);
					currentDateTimeStr = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a").format(currentDateTime);
					model.addAttribute("CurrentDateTime", currentDateTimeStr);
					msg = "Enter Captcha.";						
					urlPage = "StudentLogin";
				}
				else
				{
					userName = userName.trim();
					userName = userName.toUpperCase();
					sessioncaptchaString = sessioncaptchaString.trim();
					
					if (testStatus == 2)
					{
						checkFlag2 = 1;
					}
					else if (captchaString.equals(sessioncaptchaString))
					{
						checkFlag2 = 1;
					}
					else
					{
						msg = "Invalid Captcha.";
					}
				}
			}
						
			//Checking whether the Register No. is existed or not
			if (checkFlag2 == 1)
			{
				if (testStatus == 2)
				{
					lcObjList = semesterMasterService.getStudentLoginDetailByRegisterNumber2(userName);
				}
				else
				{
					lcObjList = semesterMasterService.getStudentLoginDetailByUserName(userName);
				}
				if (!lcObjList.isEmpty())
				{	
					for (Object[] e: lcObjList)
					{
						registerNo = e[0].toString();
						studentName = e[2].toString();
						specId = Integer.parseInt(e[4].toString());
						specCode = e[5].toString();
						specDesc = e[6].toString();
						groupId = Integer.parseInt(e[7].toString());
						programGroupCode = e[8].toString();
						programGroupMode = e[10].toString();
						programDuration = Integer.parseInt(e[11].toString());
						costCenterId = Integer.parseInt(e[13].toString());
						costCentreCode = e[14].toString();
						studyStartYear = Integer.parseInt(e[16].toString());
						studentStudySystem = e[17].toString();
						dbPassWord = e[18].toString();
						eduStatus = e[19].toString();
						lockStatus = Integer.parseInt(e[20].toString());
						feeId = (e[21] != null) ? Integer.parseInt(e[21].toString()) : 0;
						studentGraduateYear = studyStartYear + programDuration;
						eduStatusExpn = e[22].toString();
												
						if (testStatus == 2)
						{
							studEMailId = "NONE";	//Testing Purpose
						}
						else
						{
							studEMailId = (e[23] != null) ? e[23].toString() : "NONE";
						}
						
						break;
					}
					
					/*System.out.println("registerNo: "+ registerNo +" | studentName: "+ studentName 
							+" | specId: "+ specId +" | specCode: "+ specCode +" | specDesc: "+ specDesc 
							+" | groupId: "+ groupId +" | programGroupCode: "+ programGroupCode 
							+" | programGroupMode: "+ programGroupMode +" | programDuration: "+ programDuration 
							+" | costCentreCode: "+ costCentreCode +" | studyStartYear: "+ studyStartYear 
							+" | studentStudySystem: "+ studentStudySystem +" | feeId: "+ feeId 
							+" | studentGraduateYear: "+ studentGraduateYear +" | studEMailId: "+ studEMailId);*/
																				
					//Semester Sub Id Assignment
					if (programGroupCode.equals("MBA") || programGroupCode.equals("MBA5")) 
					{
						semesterSubId = "NONE";
					}
					else if (programGroupCode.equals("RP") && costCentreCode.equals("VITBS"))
					{
						semesterSubId = "NONE";
					}
					else
					{
						semesterSubId = "VL20212205";						
					}
					//System.out.println("semesterSubId: "+ semesterSubId);

					//Semester Sub Id Details
					lcObjList.clear();
					lcObjList = semesterMasterService.getSemesterDetailBySemesterSubId2(semesterSubId);
					if(!lcObjList.isEmpty())
					{
						for (Object[] e :lcObjList)
						{
							semesterId = Integer.parseInt(e[0].toString());
							academicYear = Integer.parseInt(e[5].toString());
							academicGraduateYear = Integer.parseInt(e[6].toString());
							break;
						}
						classGroupId = "CECA";
					}
					//System.out.println("semesterId: "+ semesterId +" | academicYear: "+ academicYear 
					//		+" | academicGraduateYear: "+ academicGraduateYear +" | classGroupId: "+ classGroupId);
					
					//Student Credit Transfer Detail
					oldRegNo = semesterMasterService.getStudentCreditTransferOldRegisterNumberByRegisterNumber(registerNo);					
					if ((oldRegNo != null) && (!oldRegNo.equals("")))
					{
						registerNumberList.add(registerNo);
						registerNumberList.add(oldRegNo);
						
						registerNumberArray[0] = registerNo;
						registerNumberArray[1] = oldRegNo;
					}
					else
					{
						registerNumberList.add(registerNo);
						
						registerNumberArray[0] = registerNo;
						registerNumberArray[1] = "";
					}
					//System.out.println("registerNumberList: "+ registerNumberList +" | registerNumberArray: "+ registerNumberArray.toString());
					
					//Get the ExemptionStatus
					exemptionStatus = registrationLogService.getRegistrationExemptionReasonTypeBySemesterSubIdAndRegisterNumber(
											semesterSubId, registerNo);
					
					//Student Graduation status
					graduationStatus = studentHistoryService.getGraduationValue(registerNumberList);
					
					//Student study system (i.e. FFCS or CAL or General)
					if (programGroupMode.equals("Twinning (1 Year)") || programGroupMode.equals("Twinning (2 Year)") 
							|| programGroupMode.equals("Twinning (3 Year)"))
					{
						programGroupMode = "Twinning";
					}
					
					registrationMethod = semesterMasterService.getRegistrationMethodByStudentSpecIdAndStartYear(specId, studyStartYear);
					if ((programGroupMode.equals("Twinning")) && studentStudySystem.equals("CAL") 
							&& registrationMethod.equals("GEN"))
					{
						registrationMethod = "CAL";
					}
					else if (studentStudySystem.equals("FFCS") && registrationMethod.equals("CAL"))
					{
						studentStudySystem = "CAL";
					}
					
					//studentSemester = courseRegCommonFn.findStudentSemester(programGroupCode, studyStartYear);
					
					//Regular Flag Assignment
					regularFlag = courseRegCommonFn.getCourseStatusOrCount(1, programGroupCode, specCode, studentGraduateYear, 
									academicGraduateYear, semesterId, semesterSubId, studyStartYear);
					reRegFlag = courseRegCommonFn.getCourseStatusOrCount(2, programGroupCode, specCode, studentGraduateYear, 
									academicGraduateYear, semesterId, semesterSubId, studyStartYear);
										
					//System.out.println("exemptionStatus: "+ exemptionStatus +" | programGroupMode: "+ programGroupMode
					//		+" | registrationMethod: "+ registrationMethod +" | studentStudySystem: "+ studentStudySystem 
					//		+" | studentSemester: "+ studentSemester
					//		+" | regularFlag: "+ regularFlag +" | reRegFlag: "+ reRegFlag);
					
					checkFlag3 = 1;
				}
				else
				{
					msg = "Invalid Username or Password.";
				}
			}
						
			//Checking whether the password is valid or not
			if (checkFlag3 == 1)
			{
				validateLogin = semesterMasterService.getUserLoginValidation(registerNo, password, dbPassWord, testStatus);
				if (validateLogin == 1)
				{
					checkFlag4 = 1;
				}
				else
				{
					msg = "Invalid Username or Password.";
				}
			}
									
			//Checking Register No. Account status
			if (checkFlag4 == 1)
			{
				if ((lockStatus == 0) && (graduationStatus == 0)) 
				{
					checkFlag5 = 1;
				}
				else if ((lockStatus == 0) && (graduationStatus > 0)) 
				{
					msg = "Your are eligible for graduation.  Not allowed to register.";
				}
				else
				{
					if ((exemptionStatus == 1) || (exemptionStatus == 2))
					{
						checkFlag5 = 1;
					}
					else
					{
						msg = "Your account is locked [Reason: "+ eduStatusExpn +"].";
					}
				}
				
				if (checkFlag5 == 1)
				{
					checkFlag5 = 2;
					if ((!eduStatus.equals("DO")) && (!eduStatus.equals("GT"))) 
					{
						checkFlag5 = 1;
					}
					else
					{
						msg = "Your are not eligible for Registration.";
					}
				}
				
				if (checkFlag5 == 1)
				{
					checkFlag5 = 2;
					if (programGroupMode.equals("Regular") || programGroupMode.equals("Twinning"))
					{
						checkFlag5 = 1;
					}
					else
					{
						msg = "Only regular students are eligible for Registration.";
					}
				}
			}
						
			//Checking the Allowed Admission Year/ Programme Group/ Programme Specialization
			if (checkFlag5 == 1)
			{
				if ((studyStartYear >= 2010) && (studyStartYear <= 2018))
				{
					if (programGroupCode.equals("BTECH") || programGroupCode.equals("BBA") 
							|| programGroupCode.equals("BCA") || programGroupCode.equals("BCOM") 
							|| programGroupCode.equals("BSC") || programGroupCode.equals("BSC4")
							|| programGroupCode.equals("BDES") || programGroupCode.equals("BARCH")
							|| programGroupCode.equals("MSC5") || programGroupCode.equals("MTECH5") 
							|| programGroupCode.equals("MSSE"))
					{
						checkFlag6 = 1;
					}
					else
					{
						msg = programGroupCode +" - "+ specDesc +" students are not allowed for Registration.";
					}
				}
				else if ((studyStartYear >= 2019) && (studyStartYear < academicYear))
				{
					if (programGroupCode.equals("BTECH") || programGroupCode.equals("BBA") 
							|| programGroupCode.equals("BCA") || programGroupCode.equals("BCOM") 
							|| programGroupCode.equals("BSC") || programGroupCode.equals("BSC4")  
							|| programGroupCode.equals("BDES") || programGroupCode.equals("BARCH")
							|| programGroupCode.equals("MSC5") || programGroupCode.equals("MTECH5"))
					{
						checkFlag6 = 1;
					}
					else
					{
						msg = programGroupCode +" - "+ specDesc +" students are not allowed for Registration.";
					}
				}
				else
				{
					msg = studyStartYear +" batch students are not allowed for Registration.";
				}
			}
			
			//Checking the Registration is based on Open Hours or Scheduled Date/Time
			if (checkFlag6 == 1)
			{	
				if (regTimeCheckStatus == 2)
				{
					lcObjList.clear();
					lcObjList = semesterMasterService.getRegistrationScheduleByRegisterNumber(registerNo);
					if(!lcObjList.isEmpty())
					{
						for (Object[] e: lcObjList)
						{
							startDate = format.parse(e[0].toString());
							endDate = startDate;
							schStatus = Integer.parseInt(e[4].toString());
							
							if (regSlotCheckStatus == 1)
							{
								startTime = e[1].toString();
								endTime = e[2].toString();
								allowStartTime = e[3].toString();
							}
							
							break;
						}

						if (schStatus == 0)
						{
							checkFlag7 = 1;
						}
						else
						{
							checkFlag7 = 2;
							msg = "Your are not allowed for Course Registration.";
						}
					}
					else
					{
						checkFlag7 = 2;
						msg = "Your dont have Registration Schedule.";
					}
				}
				else
				{
					checkFlag7 = 1;
				}
				
				if (checkFlag7 == 1)
				{
					checkFlag7 = 2;
					returnVal = courseRegCommonFn.AddorDropDateTimeCheck(startDate, endDate, allowStartTime, endTime, 
									registerNo, updateStatus, ipAddress);
					statusMsg = returnVal.split("/");
					allowStatus = Integer.parseInt(statusMsg[0]);
					msg = statusMsg[1];
					//System.out.println("allowStatus: "+ allowStatus +" | msg: "+ msg);
	
					if (allowStatus == 1)
					{
						checkFlag7 = 1;
						msg = "";
					}
					else
					{
						msg = msg.replace(allowStartTime, startTime);
					}
				}
			}
								
			//Checking the Student Eligibility Criteria
			if (checkFlag7 == 1)
			{
				lcObjList.clear();
				lcObjList = semesterMasterService.getCourseEligibleProgramByProgGroupId(groupId);
				if (!lcObjList.isEmpty())
				{
					for (Object[] e :lcObjList)
					{
						courseEligible = e[0].toString();
						CGPAEligible = (e[1] != null)?e[1].toString():"";
						break;
					}
					checkFlag8 = 1;
				}
				else
				{
					msg = "Your are not eligible for registration.";	
				}
			}
						
			//Checking whether the Student is already login or not. 
			if (checkFlag8 == 1)
			{									
				if (testStatus == 2)
				{
					checkFlag9 = 1;
				}
				else
				{
					activeStatus = courseRegCommonFn.ActivePresentDateTimeCheck(registerNo);
					if (activeStatus == 1) 
					{
						checkFlag9 = 1;
					}
					else
					{
						msg = "You have already logged in (or) not properly logged out.  Try again after 5 minutes.";
					}
				}
			}
			
			//Processing the Student History Data from Examination & Checking the Student History
			if (checkFlag9 == 1)
			{
				if (historyCallStatus == 1) 
				{
					studentHistoryStatus = studentHistoryService.studentHistoryInsertProcess(registerNo, studentStudySystem);
				}
				else
				{
					studentHistoryStatus = "SUCCESS";
				}
				
				if (studentHistoryStatus.equals("SUCCESS"))
				{
					lcObjList.clear();
					lcObjList = studentHistoryService.getECAStudentHistoryByEligibleGrade(registerNumberList);
					if (lcObjList.isEmpty())
					{
						checkFlag10 = 1;
					}
					else
					{
						for (Object[] e: lcObjList)
						{
							msg = "You already studied one of the ECA course and obtained the "+ e[0].toString() 
									+" grade.  Only one ECA course with pass grade was allowed to register within "
									+"your programme duration.";
							break;
						}
					}
				}
				else
				{
					msg = "You dont have grade history to proceed for registration.";
				}
			}		
			
			if (checkFlag10 == 1)
			{
				//Getting Curriculum Detail
				lcObjList.clear();
				lcObjList = programmeSpecializationCurriculumCreditService.getMaxVerDetailBySpecIdAndAdmYear2(specId, studyStartYear);
				if (!lcObjList.isEmpty())
				{
					for (Object[] e :lcObjList)
					{
						cclVersion = Float.parseFloat(e[0].toString());
						cclTotalCredit = Integer.parseInt(e[5].toString());
						checkCourseSystem = e[8].toString();
						break;
					}
				}
				//System.out.println("curriculumVersion: "+ cclVersion +" | cclTotalCredit: "+ cclTotalCredit 
				//		+" | checkCourseSystem: "+ checkCourseSystem);
								
				if (programGroupCode.equals("RP") || programGroupCode.equals("IEP"))
				{
					courseSystem[0] = "FFCS";
					courseSystem[1] = "CAL";
				}
				else
				{
					if ((checkCourseSystem != null) && (!checkCourseSystem.equals("")) 
							 && (!checkCourseSystem.equals("FFCS")) && (!checkCourseSystem.equals("NONFFCS")))
					{
						courseSystem[0] = checkCourseSystem;
						courseSystem[1] = "NONE";
					}
					else
					{
						courseSystem[0] = "FFCS";
						courseSystem[1] = "NONE";
					}
				}
				
				if ((registrationMethod.equals("GEN")) && (!programGroupCode.equals("RP")) 
						&& (!programGroupCode.equals("IEP")))
				{
					registrationMethod = "FFCS";
				}
				//System.out.println("courseSystem: "+ courseSystem +" | registrationMethod (After): "+ registrationMethod);
				
				if (checkCourseSystem.equals("NONFFCS"))
				{
					msg = "You are not eliible for Extra curricular activity course registration";
				}
				else if (checkCourseSystem.equals("FFCS"))
				{
					checkFlag11 = 1;
				}
				else
				{
					lcObjList.clear();
					lcObjList = programmeSpecializationCurriculumDetailService.getExtraCurricularCourse(
										specId, studyStartYear, cclVersion);
					if(!lcObjList.isEmpty())
					{
						checkFlag11 = 1;
					}
					else
					{
						msg = "Extra curricular activity courses are not available for your programme curriculum.  "+
								"So, not allowed for Registration";
					}
				}
			}
			/*System.out.println("checkFlag: "+ checkFlag +" | checkFlag2: "+ checkFlag2 +" | checkFlag3: "+ checkFlag3 
					+" | checkFlag4: "+ checkFlag4 +" | checkFlag5: "+ checkFlag5 +" | checkFlag6: "+ checkFlag6 
					+" | checkFlag7: "+ checkFlag7 +" | checkFlag8: "+ checkFlag8 +" | checkFlag9: "+ checkFlag9 
					+" | checkFlag10: "+ checkFlag10 +" | checkFlag11: "+ checkFlag11);*/
			
			if ((checkFlag == 1) && (checkFlag2 == 1) && (checkFlag3 == 1) && (checkFlag4 == 1) 
					&& (checkFlag5 == 1) && (checkFlag6 == 1) && (checkFlag7 == 1) && (checkFlag8 == 1) 
					&& (checkFlag9 == 1) && (checkFlag10 == 1) && (checkFlag11 == 1))
			{				
				String studentDetails = registerNo +" - "+ studentName +" - "+ specCode +" - "+ specDesc +" - "+ programGroupCode;
				
				//Eligible program id split
				egbProgram =  courseEligible.split("/");
				for (String e: egbProgram)
				{
					egbProgramInt.add(Integer.parseInt(e));									
				}
				//System.out.println("egbProgram: "+ egbProgram +" | egbProgramInt: "+ egbProgramInt);
												
				//Fixing the Minimum & Maximum credit
				String[] creditLimitArr = courseRegCommonFn.getMinimumAndMaximumCreditLimit(semesterSubId, registerNo, 
												programGroupCode, costCentreCode, studyStartYear, studentGraduateYear, 
												academicGraduateYear, semesterId, specCode).split("\\|");
				minCredit = Integer.parseInt(creditLimitArr[0]);
				maxCredit = Integer.parseInt(creditLimitArr[1]);
				//System.out.println("minCredit: "+ minCredit +" | maxCredit: "+ maxCredit);
												
				currentDateTimeStr = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a").format(currentDateTime);	

				
								
				//Login status update
				lcObjList.clear();
				lcObjList = registrationLogService.getRegistrationLogByRegisterNumber(registerNo);
				if (lcObjList.isEmpty())
				{
					registrationLogService.AddRegistrationLog(registerNo, ipAddress);
				}
				else
				{
					registrationLogService.UpdateLoginTimeStamp2(ipAddress, registerNo);
				}
				
				//To get the student category
				lcObjList.clear();
				lcObjList = semesterMasterService.getStudentFeeCategoryByFeeIdAndSpecId(feeId, specId);
				if (!lcObjList.isEmpty())
				{
					for (Object[] e: lcObjList)
					{
						studentCategory = e[3].toString();
						break;
					}
				}
				//System.out.println("studentCategory: "+ studentCategory);
				
				//To get the Student CGPA Detail. 1- Dynamic/ 2- Static
				//Data: Credit Registered | Credit Earned | CGPA
				if (cgpaStatus == 1)
				{										
					studentCgpaData = studentHistoryService.studentCGPA(registerNo, specId, studentStudySystem);
					if (((studentCgpaData == null) || (studentCgpaData.equals(""))) && (registerNumberList.size() >= 2))
					{
						studentCgpaData = studentHistoryService.studentCGPA(oldRegNo, specId, studentStudySystem);
					}
				}
				else
				{
					lcObjList.clear();
					lcObjList = studentHistoryService.getStaticStudentCGPAFromTable(registerNo, specId);
					if ((lcObjList.isEmpty()) && (registerNumberList.size() >= 2))
			    	{
						lcObjList.clear();
						lcObjList = studentHistoryService.getStaticStudentCGPAFromTable(oldRegNo, specId);
			    	}
					
					if (!lcObjList.isEmpty())
			    	{
						for (Object[] e: lcObjList)
						{
							studentCgpaData = Float.parseFloat(e[0].toString()) +"|"+ Float.parseFloat(e[1].toString()) 
													+"|"+ Float.parseFloat(e[2].toString());
							break;
						}
			    	}
			    }
				//System.out.println("studentCgpaData: "+ studentCgpaData);
												
				//Check & Assign the Compulsory Courses
				if (compulsoryCourseStatus == 1)
				{
					compulsoryCourseList = compulsoryCourseConditionDetailService.getEligibleCompulsoryCourseList(
												semesterSubId, groupId, studyStartYear, specId, registerNumberList, 
												specCode, costCenterId, studentSemester);
				}
				//System.out.println("compulsoryCourseList :"+ compulsoryCourseList);
								
				
				//Cookie assignment
				Cookie cookie = new Cookie("RegisterNumber", registerNo);
				cookie.setSecure(true);
				cookie.setHttpOnly(true);
				cookie.setMaxAge(-1);
				response.addCookie(cookie);
								
				//Session assignment
				session.setMaxInactiveInterval(15 * 60);
				
				if (egbProgramInt.size()>0)
				{
					session.setAttribute("EligibleProgramLs", egbProgramInt);									
				}
				
				session.setAttribute("RegisterNumber", registerNo);
				session.setAttribute("registerNumberList", registerNumberList);
				session.setAttribute("registerNumberArray", registerNumberArray);
				session.setAttribute("ProgramSpecId", specId);
				session.setAttribute("ProgramSpecCode", specCode);
				
				session.setAttribute("ProgramGroupId", groupId);
				session.setAttribute("ProgramGroupCode", programGroupCode);				
				session.setAttribute("StudyStartYear", studyStartYear);
				session.setAttribute("StudentGraduateYear", studentGraduateYear);
				session.setAttribute("IpAddress", ipAddress);
												
				session.setAttribute("SemesterSubId", semesterSubId);
				session.setAttribute("SemesterId", semesterId);
				session.setAttribute("OldRegNo", oldRegNo);
				session.setAttribute("registrationMethod", registrationMethod);
				session.setAttribute("minCredit", minCredit);
				session.setAttribute("maxCredit", maxCredit);
				
				session.setAttribute("classGroupId", classGroupId);
				session.setAttribute("StudySystem", courseSystem);				
				session.setAttribute("EligibleProgram", courseEligible);
				session.setAttribute("CGPAProgram", CGPAEligible);				
				session.setAttribute("curriculumVersion", cclVersion);
				
				session.setAttribute("cclTotalCredit", cclTotalCredit);
				session.setAttribute("studentDetails", studentDetails);
				session.setAttribute("studentStudySystem", studentStudySystem);				
				session.setAttribute("programGroupMode", programGroupMode);
				session.setAttribute("studentCategory", studentCategory);
				
				session.setAttribute("studentCgpaData", studentCgpaData);
				session.setAttribute("studentDetails", studentDetails);
				session.setAttribute("costCentreCode", costCentreCode);
				session.setAttribute("compulsoryCourseList", compulsoryCourseList);
				session.setAttribute("startDate", startDate);
				session.setAttribute("endDate", endDate);
				session.setAttribute("startTime", startTime);
				session.setAttribute("endTime", endTime);
				
				session.setAttribute("testStatus", testStatus);
				session.setAttribute("regularFlag", regularFlag);
				session.setAttribute("reRegFlag", reRegFlag);
				session.setAttribute("PEUEAllowStatus", PEUEAllowStatus);
				session.setAttribute("approvalStatus", approvalStatus);
				session.setAttribute("waitingListStatus", waitingListStatus);
				session.setAttribute("OptionNAStatus", OptionNAStatus);
				session.setAttribute("compulsoryCourseStatus", compulsoryCourseStatus);
				session.setAttribute("studentEMailId", studEMailId);
				session.setAttribute("otpStatus", otpStatus);
				session.setAttribute("acadGraduateYear", academicGraduateYear);
								
				session.setAttribute("pageAuthKey", courseRegCommonFn.generatePageAuthKey(registerNo, 1));
				//session.setAttribute("pageAuthKey", courseRegCommonFn.generatePageAuthKey(registerNo, 2));
				session.setAttribute("corAuthStatus", "NONE");
				session.setAttribute("authStatus", "NONE");
								
				//Clear the captcha string & image
				session.setAttribute("CAPTCHA", "");
				session.setAttribute("ENCDATA", "");
				
				model.addAttribute("studySystem", courseSystem);				
				model.addAttribute("regCredit", regCredit);
				model.addAttribute("regCount", regCount);
				model.addAttribute("wlCount", wlCount);
				model.addAttribute("maxCredit", maxCredit);
				model.addAttribute("wlCredit", wlCredit);
				
				model.addAttribute("regularFlag", regularFlag);
				model.addAttribute("CurrentDateTime", currentDateTimeStr);								
				//model.addAttribute("courseCostCentre", courseCostCentre);
				model.addAttribute("studentDetails", studentDetails);
								
				model.addAttribute("startDate", new SimpleDateFormat("dd-MMM-yyyy").format(startDate));
				model.addAttribute("startTime", startTime);
				model.addAttribute("endTime", endTime);
				
				//urlPage = "mainpages/MainPage";
				urlPage = "RegistrationStart";
			}
			else
			{
				courseRegCommonFn.callCaptcha(request,response,session,model);
				currentDateTimeStr = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a").format(currentDateTime);
				urlPage = "StudentLogin";				
			}			
		}
		catch(Exception e)
		{
			//Clear the captcha string & image
			session.setAttribute("CAPTCHA", "");
			session.setAttribute("ENCDATA", "");
			
			registrationLogService.addErrorLog(e.toString(), RegErrorMethod+"CourseRegistrationLoginController", 
					"processStudentLogin", registerNo, ipAddress);
			registrationLogService.UpdateLogoutTimeStamp2(ipAddress, registerNo);
			msg = "Invalid Details.";
			courseRegCommonFn.callCaptcha(request,response,session,model);
			urlPage = "StudentLogin";
			return urlPage;
		}
		
		currentDateTimeStr = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a").format(currentDateTime);
		model.addAttribute("info", msg);
		model.addAttribute("CurrentDateTime", currentDateTimeStr);
		
		return urlPage;		
	}	
}
