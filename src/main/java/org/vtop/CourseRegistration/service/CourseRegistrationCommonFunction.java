package org.vtop.CourseRegistration.service;

import java.awt.Color;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.vtop.CourseRegistration.Common.service.CaptchaManager;
import org.vtop.CourseRegistration.Common.service.MailUtility;
import org.vtop.CourseRegistration.model.CourseAllocationModel;
import org.vtop.CourseRegistration.model.CourseCatalogModel;
import org.vtop.CourseRegistration.model.CourseEligibleModel;
import org.vtop.CourseRegistration.model.CourseEquivalancesModel;
import org.vtop.CourseRegistration.model.CourseRegistrationModel;
import org.vtop.CourseRegistration.model.CourseRegistrationWaitingModel;
import org.vtop.CourseRegistration.model.ProgrammeSpecializationCurriculumCategoryCredit;
import org.vtop.CourseRegistration.model.SemesterDetailsModel;
import org.vtop.CourseRegistration.model.SlotTimeMasterModel;
import org.vtop.CourseRegistration.model.StudentHistoryModel;


@Service
public class CourseRegistrationCommonFunction
{	
	@Autowired private CourseCatalogService courseCatalogService;
	@Autowired private CourseAllocationService courseAllocationService;
	@Autowired private CourseRegistrationService courseRegistrationService;
	@Autowired private CourseRegistrationWaitingService courseRegistrationWaitingService;
	@Autowired private StudentHistoryService studentHistoryService;
	@Autowired private RegistrationLogService registrationLogService;
	@Autowired private ProgrammeSpecializationCurriculumCreditService programmeSpecializationCurriculumCreditService;
	@Autowired private ProgrammeSpecializationCurriculumDetailService programmeSpecializationCurriculumDetailService;
	@Autowired private CourseRegistrationWithdrawService courseRegistrationWithdrawService;
	@Autowired private CourseEquivalanceRegService courseEquivalanceRegService;
	@Autowired private CompulsoryCourseConditionDetailService compulsoryCourseConditionDetailService;	
	@Autowired private SemesterMasterService semesterMasterService;
	@Autowired private CaptchaManager captchaManager;
	
	
	private static final Logger logger = LogManager.getLogger(CourseRegistrationCommonFunction.class);
	private static final int keyLength = 21;//Fixing the key length to generate hash value
	
	
	//Check Registration of course Eligibility
	public String CheckRegistrationCondition(String[] pCourseSystem, Integer pProgramGroupId, 
						String pProgramGroupCode, String pProgramSpecCode, String pSemesterSubId, 
						String pRegisterNumber,	String pOldRegisterNumber, Integer maxCredit, 
						String pCourseId, Integer pStudentStartYear, Integer pStudentGraduateYear, 
						String studStudySystem, Integer pProgramSpecId, Float pCurriculumVersion, 
						Integer PEUEStatus, String programGroupMode, String[] classGroupId, 
						String pStudentCgpaData, Integer waitingListStatus, Integer optionNAStatus, 
						List<String> studCompulsoryCourse, Integer pSemesterId, String[] classType, 
						String costCentreCode, int academicGraduateYear, int cclTotalCredit)
	{					
		int historyflag = 2, regflag = 2, compCourseFlag = 1;
		int regAllowFlag = 2, wlAllowFlag = 2, audAllowFlag = 2, rgrAllowFlag = 2, minAllowFlag = 2, 
				honAllowFlag = 2, adlAllowFlag = 2, RPEUEAllowFlag = 2, csAllowFlag = 2, RUCUEAllowFlag = 2, 
				rgrOptionFlag = 2, allCompAllowFlag = 1, rrAllowFlag = 1;
		int courseMehtodType = 1, crTpCount = 0, subCrCount = 0, wlCount = 0;
		int regularAllowStatus = 2, NGradeAllowStatus= 2, eoAllowStatus = 2, peAdlAllowStatus = 2;
				
		int flag = 2, flag2 = 2, flag3 = 2, flag4 = 2, flag5 = 2;
		int flag6 = 2, flag7 = 2, flag8 = 2, flag9 = 2, flag10 = 2;
		//int courseCredit = 0, lectureCredit = 0, practicalCredit = 0, projectCredit = 0 obtCredit = 0, 
		//		rmgCredit = 0, ueRmgCredit = 0, ccCredit = 0, ctgCredit = 0, bskObtCredit = 0, honEarnCredit = 0, 
		//		regCredit = 0, wlCredit = 0, totalRegCredit = 0, honCompCount = 0;
		
		Integer courseProgId = 0, audCount = 0, adlCount = 0, giCount = 0;
		//Integer studentSemester = 0, totCdtReg = 0, totCdtEarn = 0;
		
		float courseCredit = 0, lectureCredit = 0, practicalCredit = 0, projectCredit = 0, obtCredit = 0, rmgCredit = 0, 
				ueRmgCredit = 0, totCdtReg = 0, totCdtEarn = 0, ccCredit = 0, ctgCredit = 0, bskObtCredit = 0, 
				regCredit = 0, wlCredit = 0, totalRegCredit = 0;
		Float cgpa = 0F;
		//float honEarnCredit = 0;
				
		String courseOption = "RGR", courseType = "NONE", subCourseOption = "NONE",	subCourseType = "NONE", 
					subCourseDate = "NONE";
		String courseCode = "", ccCourseSystem = "", ceCourseId = "NIL", compCourseStatus = "NONE";
		String genericCoursetype = "", evaluationType = "", courseAltProgId = "", prerequisite = "NONE";
		String grade = "", msg = "", subCrType = "", cgpaProgGroupId = "";
		String historyCourseId = "", historyGenericCourseType = "", historyExamMonth = "", 
					historyCourseSystem = "", authKeyVal = "NONE";
		String courseCategory = "NONE", ccCourseId = "NONE", catalogType = "NONE", basketCategory = "NONE";
		
		String[] antirequisite = {"NONE"};
		CourseCatalogModel ccm = new CourseCatalogModel();
		CourseCatalogModel historyCCM = new CourseCatalogModel();
		CourseEligibleModel crsegbm = new CourseEligibleModel();
				
		List<CourseEquivalancesModel> cemList = new ArrayList<CourseEquivalancesModel>();
		List<StudentHistoryModel> shmList = new ArrayList<StudentHistoryModel>();
		
		List<String> registerNumber2 = new ArrayList<String>();
		List<String> historyCourseTypeList = new ArrayList<String>();
		List<String> subCrTypeList = new ArrayList<String>();
		List<String> courseTypeList = new ArrayList<String>();
		List<String> courseTypeList2 = new ArrayList<String>();
		List<String> ceCourseList = new ArrayList<String>();
		List<String> ceList = new ArrayList<String>();
		List<String> spsRegList = new ArrayList<String>();
		List<String> ncCourseList = new ArrayList<String>();
		//List<String> englishCourseList = new ArrayList<String>();
				
		List<Object[]> shmList2 = new ArrayList<Object[]>();
		List<Object[]> psRegList = new ArrayList<Object[]>();
		List<Object[]> ccCreditList = new ArrayList<Object[]>();
		//System.out.println("pRegisterNumber: "+ pRegisterNumber +" | pOldRegisterNumber: "+ pOldRegisterNumber);
		
		try
		{							
			if ((pOldRegisterNumber != null) && (!pOldRegisterNumber.equals("")))
			{
				registerNumber2.add(pRegisterNumber);
				registerNumber2.add(pOldRegisterNumber);
			}
			else
			{
				registerNumber2.add(pRegisterNumber);
			}
			
			//Student CGPA Detail - Method 2
			if ((pStudentCgpaData != null) && (!pStudentCgpaData.equals("")))
	    	{
				String[] studentCgpaArr = pStudentCgpaData.split("\\|");
				
				//totCdtReg = Integer.parseInt(studentCgpaArr[0]);
		    	//totCdtEarn = Integer.parseInt(studentCgpaArr[1]);
		    	totCdtReg = Float.parseFloat(studentCgpaArr[0]);
		    	totCdtEarn = Float.parseFloat(studentCgpaArr[1]);
		    	
				cgpa = Float.parseFloat(studentCgpaArr[2]);
	    	}
			
			//studentSemester = findStudentSemester(pProgramGroupCode, pStudentStartYear);
			
			//Get the Allowed Status of Regular/ Grade Improvement/ Re-register/ Extra Option Courses/ Additional
			regularAllowStatus = getCourseStatusOrCount(1, pProgramGroupCode, pProgramSpecCode, pStudentGraduateYear, 
									academicGraduateYear, pSemesterId, pSemesterSubId, pStudentStartYear);
			NGradeAllowStatus = getCourseStatusOrCount(2, pProgramGroupCode, pProgramSpecCode, pStudentGraduateYear, 
									academicGraduateYear, pSemesterId, pSemesterSubId, pStudentStartYear);
			eoAllowStatus = getCourseStatusOrCount(3, pProgramGroupCode, pProgramSpecCode, pStudentGraduateYear, 
								academicGraduateYear, pSemesterId, pSemesterSubId, pStudentStartYear);
			peAdlAllowStatus = getCourseStatusOrCount(4, pProgramGroupCode, pProgramSpecCode, pStudentGraduateYear, 
									academicGraduateYear, pSemesterId, pSemesterSubId, pStudentStartYear);
												
			//Assigning English Course List
			//englishCourseList.addAll(Arrays.asList("ENG1000","ENG2000","ENG3000","ENG1901","ENG1902","ENG1903",
			//		"ENG1911","ENG1912","ENG1913"));
												
			//Checking the select course is valid or not
			if ((pCourseId != null) && (!pCourseId.equals("")))
			{
				ccm = courseCatalogService.getOne(pCourseId);
				if (ccm != null)
				{
					courseCode = ccm.getCode();
					courseProgId = ccm.getGroupId();
					courseAltProgId = ccm.getGroupCode();
					genericCoursetype = ccm.getGenericCourseType();
					ccCourseSystem = ccm.getCourseSystem();
					evaluationType = ccm.getEvaluationType();
					lectureCredit = ccm.getLectureCredits();
					practicalCredit = ccm.getPracticalCredits();
					projectCredit = ccm.getProjectCredits();
					courseCredit = ccm.getCredits();
					
					if ((ccm.getPrerequisite() != null) && (!ccm.getPrerequisite().equals("")))
					{
						prerequisite = ccm.getPrerequisite().replace(" ","");
					}
					
					if ((ccm.getAntirequisite() != null) && (!ccm.getAntirequisite().equals("")))
					{
						antirequisite = ccm.getAntirequisite().replace(" ","").split("/");
					}
													
					//To get the course eligible
					crsegbm = semesterMasterService.getCourseEligibleByProgramGroupId(pProgramGroupId);
					if (crsegbm != null)
			    	{
						cgpaProgGroupId = crsegbm.getProgramCgpa();
			    	}
					
					//To get the course equivalence
					cemList = semesterMasterService.getCourseEquivalancesByCourseId(pCourseId);
					for (CourseEquivalancesModel e : cemList)
					{
						ceCourseList.add(e.getCourseEquivalancesPkId().getEquivalentCourseId());
					}
					
					//To get course category from curriculum
					if (pCurriculumVersion > 0)
					{
						psRegList.clear();
						psRegList = programmeSpecializationCurriculumDetailService.getCurriculumByAdmsnYearCCVersionAndCourseCode(
										pProgramSpecId, pStudentStartYear, pCurriculumVersion, courseCode);
						if (!psRegList.isEmpty())
				    	{
							for (Object[] e: psRegList)
							{
								courseCategory = e[0].toString();
								catalogType = e[1].toString();
								ccCourseId = e[2].toString();
								basketCategory = e[5].toString();
								//ccCredit = Integer.parseInt(e[6].toString());
								ccCredit = Float.parseFloat(e[6].toString());
								break;
							}
				    	}
						else
						{
							courseCategory = "UE";
						}
					}
					
					//To assign the All Component Allow Flag
					if (evaluationType.equals("TARP") || evaluationType.equals("LSM") || evaluationType.equals("IIP"))
					{
						allCompAllowFlag = 2;
					}
					else if (ccCourseSystem.equals("FFCS"))
					{
						allCompAllowFlag = 2;
					}
										
					flag = 1;
					
					//To check whether selected course is restricted English course under UE Category
					/*if (flag == 1)
					{
						if ((pStudentStartYear <= 2018) && courseCategory.equals("UE") && englishCourseList.contains(courseCode))
						{
							flag = 2;
							msg = "Selected course "+ courseCode +" is not allowed for registration under "
									+ courseCategory +" courseCategory.";
						}
						else
						{
							flag = 1;
						}
					}*/
				}
				else
				{
					msg = "Invalid course code";
				}
				
				if (flag == 1)
				{					
					if ((pProgramGroupCode.equals("RP")) || (pProgramGroupCode.equals("IEP")))
					{
						flag = 1;
					}
					else if ((!studStudySystem.equals("CAL")) && (!studStudySystem.equals("RCAL")))
					{
						flag = 1;
					}
					else if (courseCategory.equals("PC") || courseCategory.equals("UC") || courseCategory.equals("NONE"))
					{
						flag = 1;
					}
					else
					{
						if (PEUEStatus == 1)
						{
							flag = 1;
						}
						else if (studCompulsoryCourse.contains(courseCode))
						{
							flag = 1;
						}
						else
						{
							flag = 2;
							msg = "Only PC and UC category courses are allowed for registration.";
						}
					}
				}
			}
			else
			{
				msg = "Invalid course code";
			}
			
			// Checking whether student is already registered or not
			if (flag == 1)
			{
				psRegList.clear();
				psRegList = courseRegistrationService.getRegistrationAndWLByRegisterNumberAndCourseCode(pSemesterSubId, 
								pRegisterNumber, courseCode);
				if (psRegList.isEmpty())
				{
					flag2 = 1;
				}
				else
				{
					for (Object[] e: psRegList)
					{
						msg = (e[0].toString().equals("WL"))?"Selected course is already registered in Waiting List." 
								: "Selected course is already registered."; 
						break;
					}
					flag2 = 2;
				}
				
				if (flag2 == 1)
				{
					psRegList.clear();
					psRegList = courseRegistrationService.getCERegistrationAndWLByRegisterNumberAndCourseCode(pSemesterSubId, 
									pRegisterNumber, courseCode);
					if (psRegList.isEmpty())
					{
						flag2 = 1;
					}
					else
					{
						for (Object[] e: psRegList)
						{
							msg = (e[0].toString().equals("WL"))?"Selected course is already registered in Waiting List "
									+"under course equivalance ("+ e[1].toString() +" - "+ e[2].toString() +")." 
									: "Selected course is already registered under course equivalance ("+ e[1].toString() 
									+" - "+ e[2].toString() +").";  
							break;
						}
						flag2 = 2;
					}
				}
				
				if (flag2 == 1)
				{
					psRegList.clear();
					psRegList = courseRegistrationService.getPrevSemCourseRegistrationByRegisterNumber3(registerNumber2, 
										courseCode);
					if (psRegList.isEmpty())
					{
						flag2 = 1;
					}
					else
					{
						for (Object[] e: psRegList)
						{
							msg = "You already registered this course in "+ e[1].toString() +" and the result not yet declared.  "
									+"Not allowed to register."; 
							break;
						}
						flag2 = 2;
					}
				}
								
				if (flag2 == 1)
				{
					psRegList.clear();
					psRegList = courseRegistrationService.getPrevSemCourseRegistrationCEByRegisterNumber3(registerNumber2, 
									courseCode);
					if (psRegList.isEmpty())
					{
						flag2 = 1;
					}
					else
					{
						for (Object[] e: psRegList)
						{
							msg = "You already registered this course in "+ e[1].toString() +" under course equivalance and "
									+"the result not yet declared.  Not allowed to register."; 
							break;
						}
						flag2 = 2;
					}
				}
				
				if (flag2 == 1)
				{
					psRegList.clear();
					psRegList = studentHistoryService.getArrearRegistrationByRegisterNumberAndCourseCode3(registerNumber2, 
									courseCode);
					if (psRegList.isEmpty())
					{
						flag2 = 1;
					}
					else
					{
						for (Object[] e: psRegList)
						{
							msg = "You already registered this Course in "+ e[1].toString() +" "+ e[2].toString() 
									+".  Not allowed to register."; 
							break;
						}
						flag2 = 2;
					}
				}
				
				if (flag2 == 1)
				{
					psRegList.clear();
					psRegList = studentHistoryService.getArrearCERegistrationByRegisterNumberAndCourseCode3(registerNumber2, 
									courseCode);
					if (psRegList.isEmpty())
					{
						flag2 = 1;
					}
					else
					{
						for (Object[] e: psRegList)
						{
							msg = "You already registered this Course in "+ e[1].toString() +" "+ e[2].toString() 
									+" under Course Equivalence.  Not allowed to register."; 
							break;
						}
						flag2 = 2;
					}
				}
				
				if (flag2 == 1)
				{
					psRegList.clear();
					psRegList = studentHistoryService.getCourseChangeHistoryByRegisterNumberAndCourseCode2(registerNumber2, 
										courseCode);
					if (psRegList.isEmpty())
					{
						flag2 = 1;
					}
					else
					{
						msg = "You already substituted this course with other course.  Not allowed to register.  "
								+"Kindly check your grade history.";
						flag2 = 2;
					}
				}
				
				if (flag2 == 1)
				{
					psRegList.clear();
					psRegList = courseEquivalanceRegService.getByRegisterNumberAndCourseCode(pSemesterSubId, registerNumber2, 
									Arrays.asList("CS", "CSUPE"), courseCode);
					if (psRegList.isEmpty())
					{
						flag2 = 1;
					}
					else
					{
						for (Object[] e : psRegList)
						{
							msg = "You already registered this course as "+ e[6].toString() +" in previous or current semester.  "
									+"Not allowed to register.  Kindly check your Course Registration.";
							break;
						}
						flag2 = 2;
					}
				}
			}
			
			//Checking whether the student is already studied or not
			if (flag2 == 1)
			{
				historyflag = 2;
				courseMehtodType = 1;
				shmList2.clear();
								
				shmList2 = studentHistoryService.getStudentHistoryGrade2(registerNumber2, courseCode);
				if (!shmList2.isEmpty())
				{
					for (Object[] e: shmList2)
					{
						grade = e[0].toString();
						historyCourseId = e[1].toString();
						historyGenericCourseType = e[3].toString();
						historyExamMonth = e[4].toString();
						break;
					}
					
					if (allCompAllowFlag == 1)
					{
						historyCCM = courseCatalogService.getOne(historyCourseId);
						if (historyCCM != null)
						{
							historyCourseSystem = historyCCM.getCourseSystem();
						}
						if (historyCourseSystem.equals("FFCS"))
						{
							allCompAllowFlag = 2;
						}
					}
					historyflag = 1;
				}
				
				if (historyflag == 2)
				{
					shmList2.clear();
					shmList2 = studentHistoryService.getStudentHistoryCEGrade3(registerNumber2, courseCode);
					if (!shmList2.isEmpty())
					{
						for (Object[] e: shmList2)
						{
							grade = e[0].toString();
							historyCourseId = e[1].toString();
							historyGenericCourseType = e[3].toString();
							historyExamMonth = e[4].toString();
							break;
						}
						
						if (allCompAllowFlag == 1)
						{
							historyCCM = courseCatalogService.getOne(historyCourseId);
							if (historyCCM != null)
							{
								historyCourseSystem = historyCCM.getCourseSystem();
							}
							if (historyCourseSystem.equals("FFCS"))
							{
								allCompAllowFlag = 2;
							}
						}
						historyflag = 1;
						courseMehtodType = 2;
					}
				}
				
				if (historyflag == 2)
				{					
					shmList2.clear();
					shmList2 = courseRegistrationWithdrawService.getByRegisterNumberAndCourseCode2(registerNumber2, courseCode);
					if (!shmList2.isEmpty())
					{
						for (Object[] e: shmList2)
						{
							grade = "W";
							courseMehtodType = Integer.parseInt(e[0].toString());
							historyCourseId = e[1].toString();
							historyGenericCourseType = e[3].toString();
							historyExamMonth = e[4].toString();
							break;
						}
						historyflag = 1;
					}
				}
				
				if (historyflag == 2)
				{					
					shmList2.clear();
					shmList2 = courseRegistrationService.getCancelCourseByRegisterNumberAndCourseCode(registerNumber2, courseCode);
					if (!shmList2.isEmpty())
					{
						for (Object[] e: shmList2)
						{
							grade = "CL";
							courseMehtodType = Integer.parseInt(e[0].toString());
							historyCourseId = e[1].toString();
							historyGenericCourseType = e[3].toString();
							historyExamMonth = e[4].toString();
							break;
						}
						historyflag = 1;
					}
				}
								
				if (historyflag == 1)
				{						
					if ((grade.equals("S")) || (grade.equals("U")) || (grade.equals("P")) || (grade.equals("Pass")))
					{
						msg = (courseMehtodType == 2)?"You already studied this course under equivalance and "
								+"obtained the maximum "+ grade +" grade.":"You already studied this course "
								+"and obtained the maximum "+ grade +" grade.";
					}
					else if ((grade.equals("A")) || (grade.equals("B")) || (grade.equals("C")) 
								|| (grade.equals("D"))	|| (grade.equals("E")))
					{						
						if (eoAllowStatus == 1)
						{
							//if (historyGenericCourseType.equals(genericCoursetype))
							//{
								if (pStudentGraduateYear <= academicGraduateYear)
								{
									courseOption = (courseMehtodType == 2) ? "GICE" : "GI";
									flag3 = 1;
								}
								else
								{
									giCount = courseRegistrationService.getGICourseCountByRegisterNumberCourseOptionAndClassGroup(
													pSemesterSubId, pRegisterNumber, classGroupId);
									if (giCount == 0)
									{
										courseOption = (courseMehtodType == 2) ? "GICE" : "GI";
										flag3 = 1;
									}
									else
									{
										flag3 = 2;
										msg = "Only one grade improvement course is allowed.";
									}
								}
							//}
							//else
							//{
							//	msg = "Grade improvement will be permitted only all course components are equal. "
							//			+"Your previous studied course component is "+ historyGenericCourseType 
							//			+" and selected course component is "+ genericCoursetype +".";
							//}
						}
						else
						{
							msg = "Grade improvement courses are not allowed to register.";
						}
					}
					else if ((grade.equals("F")) || (grade.equals("Fail")))
					{
						if (NGradeAllowStatus == 1)
						{
							courseOption = (courseMehtodType == 2) ? "RRCE" : "RR";
							flag3 = 1;
						}
						else
						{
							flag3 = 2;
							msg = "Re-register courses are not allowed to register.";
						}
					}
					else if (grade.equals("N") || grade.equals("N1") || grade.equals("N2") 
								|| grade.equals("N3") || grade.equals("N4"))
					{
						if (NGradeAllowStatus == 1)
						{
							courseOption = (courseMehtodType == 2) ? "RRCE" : "RR";
							flag3 = 1;
						}
						else
						{
							flag3 = 2;
							msg = "Re-register courses are not allowed to register.";
						}
					}
					else if (grade.equals("W"))
					{
						courseOption = (courseMehtodType == 2) ? "RWCE" : "RGW";
						flag3 = 1;
					}
					else if ((grade.equals("WWW")) || (grade.equals("AAA")))
					{
						if (NGradeAllowStatus == 1)
						{
							courseOption = (courseMehtodType == 2) ? "RRCE" : "RR";
							flag3 = 1;
						}
						else
						{
							flag3 = 2;
							msg = "Re-register courses are not allowed to register.";
						}
					}
					else if (grade.equals("---"))
					{
						msg = "This is your Re-FAT course, so not allowed to register.";
					}
					else if ((grade == null) || (grade.equals("")) || (grade.equals("-")))
					{
						msg = "This is your credit transfer course, so not allowed to register.";
					}
					else if (grade.equals("CL"))
					{
						courseOption = (courseMehtodType == 2) ? "RPCE" : "RGP";
						flag3 = 1;
					}
				}
				else
				{	
					if ((courseOption.equals("RGR")) && (pProgramGroupCode.equals("RP")))
					{
						courseOption = "RGP";
					}
					else if ((courseOption.equals("RGR")) && (!pProgramGroupCode.equals("IEP")) 
								&& (studStudySystem.equals("FFCS")) && (ccCourseSystem.equals("CAL")))
					{
						courseOption = "RGCE";
					}
					flag3 = 1;
				}
								
				if (flag3 == 1)
				{
					if ((!pProgramGroupCode.equals("IEP")) && (studStudySystem.equals("FFCS")) 
							&& (ccCourseSystem.equals("CAL")) && (courseOption.equals("RGCE") 
									|| courseOption.equals("RWCE") || courseOption.equals("RPCE")))
					{
						shmList2.clear();
						shmList2 = semesterMasterService.getCourseEquivalanceListByCourseCode(courseCode);
						if (!shmList2.isEmpty())
						{
							for (Object[] e: shmList2)
							{							
								if (e[6].toString().equals("FFCS"))
								{
									historyCourseId = e[2].toString();
									historyGenericCourseType = e[4].toString();
									historyExamMonth = e[5].toString();
									break;
								}
								else if (e[2] != null)
								{
									historyCourseId = e[2].toString();
									historyGenericCourseType = e[4].toString();
									historyExamMonth = e[5].toString();
								}
								else
								{
									historyCourseId = e[0].toString();
									historyGenericCourseType = genericCoursetype;
									historyExamMonth = e[5].toString();
								}
							}
						}
					}
										
					if ((allCompAllowFlag == 1) && ((courseOption.equals("RR")) || (courseOption.equals("RRCE"))))
					{
						historyCourseTypeList.clear();
						
						if ((historyGenericCourseType.equals("ETLP")) || (historyGenericCourseType.equals("ETL")) 
								|| (historyGenericCourseType.equals("ETP")) || (historyGenericCourseType.equals("ELP")))
						{						
							historyCourseTypeList = studentHistoryService.getStudentHistoryFailComponentCourseType(registerNumber2, 
														historyCourseId, historyExamMonth);
							if (historyCourseTypeList.isEmpty())
							{
								historyCourseTypeList = semesterMasterService.getCourseTypeComponentByGenericType(historyGenericCourseType);
							}
						}
						else
						{
							historyCourseTypeList = semesterMasterService.getCourseTypeComponentByGenericType(historyGenericCourseType);
						}
						
						if (historyGenericCourseType.equals(genericCoursetype))
						{
							courseTypeList = historyCourseTypeList;
						}
						else
						{
							courseTypeList2 = historyCourseTypeList;
							
							if (courseTypeList2.size() > 0)
							{
								for (String rrCourseType : courseTypeList2)
								{
									if ((genericCoursetype.equals("ETLP")) && (historyGenericCourseType.equals("ETL")) 
											&& ((rrCourseType.equals("ETH")) || (rrCourseType.equals("ELA"))))
									{
										courseTypeList.add(rrCourseType);
									}
									else if ((genericCoursetype.equals("ETLP")) && (historyGenericCourseType.equals("ETP")) 
											&& ((rrCourseType.equals("ETH")) || (rrCourseType.equals("EPJ"))))
									{
										courseTypeList.add(rrCourseType);
									}
									else if ((genericCoursetype.equals("ETLP")) && (historyGenericCourseType.equals("ELP")) 
											&& ((rrCourseType.equals("ELA")) || (rrCourseType.equals("EPJ"))))
									{
										courseTypeList.add(rrCourseType);
									}
									else if ((genericCoursetype.equals("ETLP")) && (historyGenericCourseType.equals("TH")) 
											&& (rrCourseType.equals("TH")))
									{
										courseTypeList.add("ETH");
									}
									else if ((genericCoursetype.equals("ETLP")) && (historyGenericCourseType.equals("LO")) 
											&& (rrCourseType.equals("LO")))
									{
										courseTypeList.add("ELA");
									}
									else if ((genericCoursetype.equals("ETL")) && (historyGenericCourseType.equals("ETLP")) 
											&& ((rrCourseType.equals("ETH")) || (rrCourseType.equals("ELA"))))
									{
										courseTypeList.add(rrCourseType);
									}
									else if ((genericCoursetype.equals("ETL")) && (historyGenericCourseType.equals("ETP")) 
											&& (rrCourseType.equals("ETH")))
									{
										courseTypeList.add(rrCourseType);
									}
									else if ((genericCoursetype.equals("ETL")) && (historyGenericCourseType.equals("ELP")) 
											&& (rrCourseType.equals("ELA")))
									{
										courseTypeList.add(rrCourseType);
									}
									else if ((genericCoursetype.equals("ETL")) && (historyGenericCourseType.equals("TH")) 
											&& (rrCourseType.equals("TH")))
									{
										courseTypeList.add("ETH");
									}
									else if ((genericCoursetype.equals("ETL")) && (historyGenericCourseType.equals("LO")) 
											&& (rrCourseType.equals("LO")))
									{
										courseTypeList.add("ELA");
									}
									else if ((genericCoursetype.equals("ETP")) && (historyGenericCourseType.equals("ETLP")) 
											&& ((rrCourseType.equals("ETH")) || (rrCourseType.equals("EPJ"))))
									{
										courseTypeList.add(rrCourseType);
									}
									else if ((genericCoursetype.equals("ETP")) && (historyGenericCourseType.equals("ETL")) 
											&& (rrCourseType.equals("ETH")))
									{
										courseTypeList.add(rrCourseType);
									}
									else if ((genericCoursetype.equals("ETP")) && (historyGenericCourseType.equals("ELP")) 
											&& (rrCourseType.equals("EPJ")))
									{
										courseTypeList.add(rrCourseType);
									}
									else if ((genericCoursetype.equals("ETP")) && (historyGenericCourseType.equals("TH")) 
											&& (rrCourseType.equals("TH")))
									{
										courseTypeList.add("ETH");
									}
									else if ((genericCoursetype.equals("ELP")) && (historyGenericCourseType.equals("ETLP")) 
											&& ((rrCourseType.equals("ELA")) || (rrCourseType.equals("EPJ"))))
									{
										courseTypeList.add(rrCourseType);
									}
									else if ((genericCoursetype.equals("ELP")) && (historyGenericCourseType.equals("ETL")) 
											&& (rrCourseType.equals("ELA")))
									{
										courseTypeList.add(rrCourseType);
									}
									else if ((genericCoursetype.equals("ELP")) && (historyGenericCourseType.equals("ETP")) 
											&& (rrCourseType.equals("EPJ")))
									{
										courseTypeList.add(rrCourseType);
									}
									else if ((genericCoursetype.equals("ELP")) && (historyGenericCourseType.equals("LO")) 
											&& (rrCourseType.equals("LO")))
									{
										courseTypeList.add("ELA");
									}
									else if ((genericCoursetype.equals("TH")) && (historyGenericCourseType.equals("ETLP")) 
											&& (rrCourseType.equals("ETH")))
									{
										courseTypeList.add("TH");
									}
									else if ((genericCoursetype.equals("TH")) && (historyGenericCourseType.equals("ETL")) 
											&& (rrCourseType.equals("ETH")))
									{
										courseTypeList.add("TH");
									}
									else if ((genericCoursetype.equals("TH")) && (historyGenericCourseType.equals("ETP")) 
											&& (rrCourseType.equals("ETH")))
									{
										courseTypeList.add("TH");
									}
									else if ((genericCoursetype.equals("LO")) && (historyGenericCourseType.equals("ETLP")) 
											&& (rrCourseType.equals("ELA")))
									{
										courseTypeList.add("LO");
									}
									else if ((genericCoursetype.equals("LO")) && (historyGenericCourseType.equals("ETL")) 
											&& (rrCourseType.equals("ELA")))
									{
										courseTypeList.add("LO");
									}
									else if ((genericCoursetype.equals("LO")) && (historyGenericCourseType.equals("ELP")) 
											&& (rrCourseType.equals("ELA")))
									{
										courseTypeList.add("LO");
									}
									else
									{
										courseTypeList.clear();
										rrAllowFlag = 2;
										break;
									}
								}
							}
						}
					}
					
					if (rrAllowFlag == 1)
					{
						flag3 = 1;
					}
					else
					{
						flag3 = 2;
						msg = "Re-Registration is permitted either the left out component is similar or all components are matching."+
								"Your previous studied failed course component is not matched with selected course component.";
					}
				}
			}
			
			// Checking the selected course is related to Student programme
			if (flag3 == 1)
			{
				if (historyflag == 1)
				{
					flag4 = 1;
				}
				else if (pProgramGroupId == courseProgId)
		        {
		            flag4 = 1;
		        }
				else if ((cgpaProgGroupId == null) || (cgpaProgGroupId.equals("")))
				{
					flag4 = 1;
				}
				else
				{
					msg = "Your are not eligible to take higher level course.";
					int pgid;
					int cgpaCourseFlag = 2;
					
					if (courseAltProgId != null)
					{
						String[] pg = courseAltProgId.split("/");
						for (int i = 0; i < pg.length; i++)
						{
							pgid = Integer.parseInt(pg[i]);
							if (pgid == pProgramGroupId)
							{
								flag4 = 1;
								msg = "";
								break;
							}
						}
					}
					
					if (flag4 == 2)
					{
						String[] pg = cgpaProgGroupId.split("/");
							
						for (int i = 0; i < pg.length; i++)
						{
							pgid = Integer.parseInt(pg[i]);
							//System.out.println("Check Level 2==> pgid: "+ pgid);
							//if (pgid == pProgramGroupId)
							if (pgid == courseProgId)
							{
								cgpaCourseFlag = 1;
								break;
							}
						}
												
						if (cgpaCourseFlag == 1) 
						{
							if (cgpa >= 8)
							{
								flag4 = 1;
								msg = "";
							}
							else
							{
								flag4 = 2;
								msg = "You don't have the required CGPA to select higher level course.";
							}
						}
						else
						{
							flag4 = 1;
							msg = "";
						}
					}
				}
			}
			
			// To check the credit limit
			if (flag4 == 1)
			{					
				ncCourseList = programmeSpecializationCurriculumDetailService.getNCCourseByYearAndCCVersion(pProgramSpecId, 
									pStudentStartYear, pCurriculumVersion);
				regCredit = courseRegistrationService.getRegCreditByRegisterNumberAndNCCourseCode(pSemesterSubId, pRegisterNumber, 
								ncCourseList);
				wlCredit = courseRegistrationWaitingService.getRegCreditByRegisterNumberAndNCCourseCode(pSemesterSubId, 
								pRegisterNumber, ncCourseList);
				wlCount = courseRegistrationWaitingService.getRegisterNumberCRWCount(pSemesterSubId, pRegisterNumber);
				totalRegCredit = regCredit + wlCredit;
				
				if (courseTypeList.size() > 0)
				{
					for (String courseType3 : courseTypeList)
					{
						if (courseType.equals("NONE"))
						{
							courseType = courseType3;
						}
						else
						{
							courseType = courseType +","+ courseType3;
						}
						
						if (courseType3.equals("ETH"))
						{
							totalRegCredit = totalRegCredit + lectureCredit;
						}
						else if (courseType3.equals("ELA"))
						{
							totalRegCredit = totalRegCredit + practicalCredit;
						}
						else if (courseType3.equals("EPJ"))
						{
							totalRegCredit = totalRegCredit + projectCredit;
						}
						else
						{
							totalRegCredit = totalRegCredit + courseCredit;
						}
						
						crTpCount++;
					}
				}
				else
				{
					totalRegCredit = totalRegCredit + courseCredit;
				}
				
				//regAllowFlag = 1;
				if ((pStudentGraduateYear <= academicGraduateYear) && (maxCredit == 30) && (totalRegCredit <= 32))
				{
					regAllowFlag = 1;
				}
				else if (totalRegCredit <= maxCredit)
				{
					regAllowFlag = 1;
				}
				
				if ((waitingListStatus == 1) && (!studCompulsoryCourse.contains(courseCode)) && (wlCount < 2))
				{
					wlAllowFlag = 1;
				}
				
				if ((!pProgramGroupCode.equals("RP")) && (!pProgramGroupCode.equals("MBA")) 
						&& (!pProgramGroupCode.equals("MBA5")) && (pStudentGraduateYear <= academicGraduateYear) 
						&& (maxCredit == 30) && ((regCredit + wlCredit) < 30) && (totalRegCredit <= 32))
				{
					flag5 = 1;
				}
				else if (totalRegCredit <= maxCredit)
				{
					flag5 = 1;
				}
				else
				{
					flag5 = 2;
					msg = "You cannot register more than "+ maxCredit +" credits.";
				}
				
				if (flag5 == 1)
				{
					if (courseOption.equals("RGR") || courseOption.equals("RGP") || courseOption.equals("RGCE") 
							|| courseOption.equals("RGA") || courseOption.equals("RPCE") || courseOption.equals("HON") 
							|| courseOption.equals("MIN") || courseOption.equals("AUD") || courseOption.equals("RGW") 
							|| courseOption.equals("RWCE") || courseOption.equals("RPEUE") || courseOption.equals("RUCUE") 
							|| courseOption.equals("DM") || courseOption.equals("RWVC") || courseOption.equals("RUEPE") 
							|| courseOption.equals("RGVC"))
					{	
						rgrOptionFlag = 1;
						flag5 = 2;
						
						if (regularAllowStatus == 1)
						{
							flag5 = 1;
						}
						else
						{
							msg = "Regular course(s) are not allowed to register.";
						}
					}
					else
					{
						flag5 = 1;
					}
				}
				
				if (flag5 == 1)
				{
					flag5 = 2;
					
					if ((rgrOptionFlag == 1) && (studCompulsoryCourse.contains(courseCode)))
					{
						compCourseStatus = compulsoryCoursePriorityCheck(pProgramGroupId, pStudentStartYear, 
												pStudentGraduateYear, pSemesterId, pSemesterSubId, pRegisterNumber, 
												classGroupId, classType, pProgramSpecCode, pProgramSpecId, 
												pProgramGroupCode, pOldRegisterNumber, studCompulsoryCourse, 
												costCentreCode, courseCode);
						if (compCourseStatus.equals("NONE") || compCourseStatus.equals("SUCCESS"))
						{
							flag5 = 1;
						}
						else
						{
							msg = compCourseStatus;
						}
					}
					else
					{
						flag5 = 1;
					}
				}
			}
			
			// To check the Anti-requisite
			if (flag5 == 1)
			{
				if ((pProgramGroupCode.equals("RP")) || (pProgramGroupCode.equals("IEP")))
				{
					flag6 = 1;
				}
				else if (historyflag == 1)
				{
					flag6 = 1;
				}
				else if (antirequisite.length <= 0)
				{
					flag6 = 1;
				}
				else
				{
					spsRegList.clear();
					shmList.clear();
					
					shmList = studentHistoryService.getStudentHistoryPARequisite(registerNumber2, antirequisite);
					if (shmList.isEmpty())
					{
						spsRegList = courseRegistrationService.getPrevSemCourseRegistrationPARequisiteByRegisterNumber(
										registerNumber2, Arrays.asList(antirequisite));
						if (spsRegList.isEmpty())
						{
							flag6 = 1;
						}
						else
						{
							msg = "You already studied or registered the Anti-Requisite course(s) of the selected course.";
						}
					}
					else
					{
						msg = "You already studied or registered the Anti-Requisite course(s) of the selected course.";
					}
				}
			}
			
			// To check the Pre-requisite
			if (flag6 == 1)
			{
				if ((pProgramGroupCode.equals("RP")) || (pProgramGroupCode.equals("IEP")))
				{
					flag7 = 1;
				}
				else if (historyflag == 1)
				{
					flag7 = 1;
				}
				else if ((prerequisite == null) || prerequisite.equals("") 
								|| prerequisite.equals("NONE"))
				{
					flag7 = 1;
				}
				else
				{
					int prereqflag = 1, eptPrFlag = 2, pcmbPrFlag = 2;
					String prCourseCode = "", eptResult = "NONE", pcmbStatus = "NONE";
					String[] prerequisite2 = {};
					String[] prerequisite3 = {};
					ceList = new ArrayList<String>();
					
					prerequisite2 = prerequisite.split(",");
					for (int i = 0; i < prerequisite2.length; i++)
					{
						prCourseCode = "";
						ceList.clear();
						shmList.clear();
						spsRegList.clear();
												
						prerequisite3 = prerequisite2[i].split("/");
						for (int j = 0; j < prerequisite3.length; j++)
						{
							prCourseCode = prerequisite3[j].trim();
							
							if (prCourseCode.equals("EPT"))
							{
								eptPrFlag = 1;
							}
							else if ((prCourseCode.equals("PCMB")) || (prCourseCode.equals("PCMC")) 
										|| (prCourseCode.equals("PCM")) || (prCourseCode.equals("PCB")) 
										|| (prCourseCode.equals("PCBE")) || (prCourseCode.equals("PCME")))
							{
								pcmbPrFlag = 1;
							}
							
							ceList.add(prCourseCode);
						}
						
						if (eptPrFlag == 1)
						{
							eptResult = semesterMasterService.getEPTResultByRegisterNumber(registerNumber2);
							if ((eptResult == null) || (eptResult.equals("")))
							{
								eptResult = "NONE";
							}
						}
						
						if (pcmbPrFlag == 1)
						{
							pcmbStatus = semesterMasterService.getPCMBStatusByRegisterNumber(registerNumber2);
							if ((pcmbStatus == null) || (pcmbStatus.equals("")) || (pcmbStatus.equals("NONE")))
							{
								pcmbStatus = semesterMasterService.getPCMBStatusFromAdmissionsByRegisterNumber(pRegisterNumber);
							}
							if ((pcmbStatus == null) || (pcmbStatus.equals("")))
							{
								pcmbStatus = "NONE";
							}
						}
												
						shmList = studentHistoryService.getStudentHistoryPARequisite2(registerNumber2, ceList);
						if (shmList.isEmpty())
						{
							if ((eptPrFlag == 1) && (eptResult.equals("P")))
							{
								prereqflag = 1;
							}
							else if ((pcmbPrFlag == 1) && (!pcmbStatus.equals("NONE")))
							{
								for (String pcmbs: ceList)
								{
									prereqflag = 2;
									if(pcmbs.equals(pcmbStatus))
									{
										prereqflag = 1;
										break;
									}
								}
							}
							else
							{
								spsRegList = courseRegistrationService.getPrevSemCourseRegistrationPARequisiteByRegisterNumber(
												registerNumber2, ceList);
								if (spsRegList.isEmpty())
								{
									prereqflag = 2;
								}
							}
						}
					}
					
					if (prereqflag == 1)
					{
						flag7 = 1;
					}
					else
					{
						msg = "You did not study the Pre-Requisite course(s) of the selected course.";
					}	
				}
			}
				
			//TARP Course Eligibility Checking
			if (flag7 == 1)
			{
				int tarpCeilCdtper = 0, tarpPercentage = 65;
				float studTarpCredits = 0, psRegCredit = 0, tarpCdtRequired = 0, tarpCdtPer = 0;
				
				if (evaluationType.equals("TARP"))
				{					
					if (historyflag == 1)
					{
						flag8 = 1;
					}
					/*else if (pProgramGroupCode.equals("BTECH"))
					{
						if (studTarpCredits >= 115)
						{
							flag8 = 1;
						}
						else
						{
							msg = "You are not eligible to take TARP course.  You earned "+ studTarpCredits 
									+" credits and the eligible credit should be greater than or equal to 115 credits";
						}
					}
					else if (pProgramGroupCode.equals("MTECH5"))
					{
						if (studTarpCredits >= 160)
						{
							flag8 = 1;
						}
						else
						{
							msg = "You are not eligible to take TARP course.  You earned "+ studTarpCredits 
									+" credits and the eligible credit should be greater than or equal to 160 credits";
						}
					}*/
					else if ((pProgramGroupCode.equals("BTECH") || pProgramGroupCode.equals("MTECH5")) && (cclTotalCredit > 0))
					{						
						psRegCredit = courseRegistrationService.getPSRegisteredTotalCreditsByRegisterNumber4(registerNumber2);
						//System.out.println("totCdtEarn: "+ totCdtEarn +" | psRegCredit: "+ psRegCredit);
						
						studTarpCredits = totCdtEarn + psRegCredit;
						tarpCdtRequired = ((float)cclTotalCredit * (float)tarpPercentage) / 100;
						tarpCdtPer = ((float)studTarpCredits / (float)cclTotalCredit) * 100;
						tarpCeilCdtper = (int) Math.ceil(tarpCdtPer);
						//System.out.println("studTarpCredits: "+ studTarpCredits +" | tarpCdtRequired: "+ tarpCdtRequired 
						//		+" | tarpCdtPer: "+ tarpCdtPer +" | tarpCeilCdtper: "+ tarpCeilCdtper);
						
						if (tarpCeilCdtper >= tarpPercentage)
						{
							flag8 = 1;
						}
						else
						{							
							msg = "You did not meet the required credits "+ tarpCdtRequired +" ("+ tarpPercentage +"%) to "
									+"take this TARP course.  Whereas, you earned "+ studTarpCredits +" ("+ tarpCeilCdtper +"%) only."
									+"\nDescription of your total credits:  Earned = "+ totCdtEarn +" | Result Not Published = "+ psRegCredit +".";
						}
					}
					else
					{
						msg = "You are not eligible to take TARP course.";
					}
				}
				else
				{
					flag8 = 1;
				}
			}
			
			//To check the eligibility of final project course
			if (flag8 == 1)
			{
				if ((genericCoursetype.equals("PJT")) && (evaluationType.equals("CAPSTONE")))
				{
					int cspeFlag = 2, cspeFlag2 = 2;
					Integer ceilCdtper = 0, maxPjtYear = 0;
					Float cdtRequired = 0F, cdtPer = 0F, totCredit = 0F, pjtPer = 0F, totPjtCdt = 0F, psRegCredit = 0F, failCdt = 0F;
					
					if (historyflag == 1)
					{
						cspeFlag = 1;
					}
					else if (pStudentGraduateYear < academicGraduateYear)
					{
						cspeFlag = 1;
					}
					else if (pStudentGraduateYear == academicGraduateYear)
					{
						if (((pSemesterId == 1) || (pSemesterId == 2) || (pSemesterId == 3))
								&& (pProgramGroupCode.equals("MTECH") || pProgramGroupCode.equals("MCA") 
										|| (pProgramGroupCode.equals("MTECH5") && pProgramSpecCode.equals("MIS"))
										|| (pProgramGroupCode.equals("MSC5") && pProgramSpecCode.equals("MSI"))))
						{
							cspeFlag = 1;
						}
						else if ((pSemesterId == 5) || (pSemesterId == 6))
						{
							cspeFlag = 1;
						}
						else
						{
							msg = "Only M.Tech., MCA and M.Tech. Integrated (MIS) students are allowed to register the cap stone project course.";
						}
					}
					else
					{
						msg = "Only final year and timed out students are allowed to register the cap stone project course.";
					}
					
					if (cspeFlag == 1)
					{
						cspeFlag = 2;
						
						if (cclTotalCredit > 0)
						{
							totCredit = (float) cclTotalCredit;
							pjtPer = 65F;
							cspeFlag = 1;
						}
						else
						{
							psRegList.clear();
							psRegList = semesterMasterService.getProjectEligibilityByProgramGroupIdAndStudYear(pProgramGroupId, pStudentStartYear);
							if (!psRegList.isEmpty())
							{
								for (Object[] e: psRegList)
								{
									totCredit = Float.parseFloat(e[0].toString());
									pjtPer = Float.parseFloat(e[1].toString());
									cspeFlag = 1;
									break;
								}
							}
							else
							{
								psRegList.clear();
								psRegList = semesterMasterService.getStudentMaxYearProjectEligibilityByProgramGroupId(pProgramGroupId);
								if (!psRegList.isEmpty())
								{
									for (Object[] e: psRegList)
									{
										maxPjtYear = Integer.parseInt(e[0].toString());
										totCredit = Float.parseFloat(e[1].toString());
										pjtPer = Float.parseFloat(e[2].toString());
										break;
									}
								}
								//System.out.println("maxPjtYear: "+ maxPjtYear +" | pStudentStartYear: "+ pStudentStartYear);
								
								if (pStudentStartYear > maxPjtYear)
								{
									cspeFlag = 1;
								}
								else
								{
									msg = "Project eligibiligy criteria does not exist for your program.  "+
						    				"So cannot register this cap stone project course.";
								}
							}
						}
					}
					//System.out.println("totCredit: "+ totCredit +" | pjtPer: "+ pjtPer);
					
				    if (cspeFlag == 1)
				    {	
				    	if (totCdtReg > 0)
				    	{								
							failCdt = Float.parseFloat(studentHistoryService.getStudentHistoryFailCourseCredits2(registerNumber2).toString());
							psRegCredit = courseRegistrationService.getPSRegisteredTotalCreditsByRegisterNumber4(registerNumber2);
																					
							totPjtCdt = totCdtEarn + failCdt + psRegCredit;							
					    	cdtRequired = ((float)totCredit * (float)pjtPer) / 100;
							cdtPer = ((float)totPjtCdt / (float)totCredit) * 100;
							ceilCdtper = (int) Math.ceil(cdtPer);
							
							/*System.out.println("totCdtReg: "+ totCdtReg +" | totCdtEarn: "+ totCdtEarn 
									+" | failCdt: "+ failCdt +" | psRegCredit: "+ psRegCredit);
							System.out.println("totPjtCdt: "+ totPjtCdt +" | cdtRequired: "+ cdtRequired 
									+" | cdtPer: "+ cdtPer +" | ceilCdtper: "+ ceilCdtper);*/
							
							/*if ((pProgramGroupCode.equals("BTECH")) && (totPjtCdt >= 115))
				    		{
								cspeFlag2 = 1;
				    		}
							else if ((!pProgramGroupCode.equals("BTECH")) && (ceilCdtper >= pjtPer))
							{
								cspeFlag2 = 1;
							}
							else
							{
								if (pProgramGroupCode.equals("BTECH"))
					    		{
									msg = "You did not meet the required credits 115 to take this cap stone project.  "
											+"Whereas, your total credits are "+ totPjtCdt +" only.\nDescription of your "
											+"total credits:  Earned = "+ totCdtEarn +" | Failed (F-N Grade) = "+ failCdt 
											+" | Current Semester = "+ psRegCredit +".";
					    		}
								else
								{
									msg = "You did not meet the required credits "+ cdtRequired +" ("+ pjtPer +"%) to "
											+"take this cap stone project.  Whereas, your total credits are "+ totPjtCdt 
											+" ("+ ceilCdtper +"%) only.\nDescription of your total credits:  "
											+"Earned = "+ totCdtEarn +" | Failed = "+ failCdt +" | Current Semester = "+ psRegCredit +".";	
								}
							}*/
							
							if (ceilCdtper >= pjtPer)
							{
								cspeFlag2 = 1;
							}
							else
							{
								msg = "You did not meet the required credits "+ cdtRequired +" ("+ pjtPer +"%) to "
										+"take this cap stone project.  Whereas, your total credits are "+ totPjtCdt 
										+" ("+ ceilCdtper +"%) only.\nDescription of your total credits:  "
										+"Earned = "+ totCdtEarn +" | Failed = "+ failCdt +" | Current Semester = "+ psRegCredit +".";
							}
				    	}
				    	else
				    	{
				    		msg = "You are not eligible to take cap stone project course.";
				    	}
				    }
				   
				    if ((cspeFlag == 1) && (cspeFlag2 == 1))
				    {
				    	flag9 = 1;
				    }
				}
				else
				{
					flag9 = 1;
				}
			}
			
			//To check the Curriculum Credit
			if (flag9 == 1)
			{
				/*System.out.println("rgrOptionFlag: "+ rgrOptionFlag + " | pProgramGroupCode: "+ pProgramGroupCode 
						+" | studStudySystem: "+ studStudySystem +" | courseOption: "+ courseOption 
						+" | courseCategory: "+ courseCategory);*/
				
				if (rgrOptionFlag == 1)
				{					
					if ((eoAllowStatus == 1) && (cgpa >= 8))
					{
						audCount = courseRegistrationService.getCourseCountByRegisterNumberAndCourseOption(pSemesterSubId, 
										pRegisterNumber, Arrays.asList("AUD"));
					}
					
					if ((eoAllowStatus == 1) || (peAdlAllowStatus == 1))
					{
						adlCount = courseRegistrationService.getCourseCountByRegisterNumberAndCourseOption(pSemesterSubId, 
										pRegisterNumber, Arrays.asList("RGA"));
					}
								
					if ((pProgramGroupCode.equals("RP")) || (pProgramGroupCode.equals("IEP")))
					{
						rgrAllowFlag = 1;
						flag10 = 1;
					}
					else if (programGroupMode.equals("Twinning"))
					{
						rgrAllowFlag = 1;
						flag10 = 1;
					}
					else if ((!studStudySystem.equals("CAL")) && (!studStudySystem.equals("RCAL")))
					{
						rgrAllowFlag = 1;
						
						if (optionNAStatus == 1)
						{
							if (courseOption.equals("RGR"))
							{	
								if ((eoAllowStatus == 1) && (cgpa >= 8) && (audCount < 1))
								{
									audAllowFlag = 1;
								}
								
								if ((eoAllowStatus == 1) && (adlCount < 1))
								{
									adlAllowFlag = 1;
								}
								
								if (cgpa >= 8)
								{
									psRegList.clear();
									psRegList = semesterMasterService.getAdditionalLearningTitleByLearnTypeGroupIdSpecIdAndCourseCode(
													1, "HON", pProgramGroupId, pProgramSpecId, courseCode, studStudySystem);
									if (!psRegList.isEmpty())
									{
										honAllowFlag = 1;
									}
									
									psRegList.clear();
									psRegList = semesterMasterService.getAdditionalLearningTitleByLearnTypeGroupIdSpecIdAndCourseCode(
													1, "MIN", pProgramGroupId, pProgramSpecId, courseCode, studStudySystem);
									if (!psRegList.isEmpty())
							    	{
										minAllowFlag = 1;
							    	}
								}
							}
																		
							psRegList.clear();
							psRegList = studentHistoryService.getStudentHistoryCS2(registerNumber2, courseCode, 
											studStudySystem, pProgramSpecId, pStudentStartYear, pCurriculumVersion, 
											pSemesterSubId, courseCategory, courseOption, ccCourseId);
							if (!psRegList.isEmpty())
							{
								csAllowFlag = 1;
							}
						}
												
						if ((rgrAllowFlag == 1) || (csAllowFlag == 1) || (honAllowFlag == 1) 
								|| (minAllowFlag == 1) || (adlAllowFlag == 1) || (audAllowFlag == 1))
						{
							flag10 = 1;
						}
						else
						{
							msg = "You already completed the credit limit or you may not eligible to provide other "
									+"options.  So not allowed to Register.";
						}
					
					}
					else if (studStudySystem.equals("CAL") || studStudySystem.equals("RCAL"))
					{
						ccCreditList = programmeSpecializationCurriculumCreditService.getCurrentSemRegCurCtgCreditByRegisterNo(
											pProgramSpecId, pStudentStartYear, pCurriculumVersion, pSemesterSubId, registerNumber2);
						if (!ccCreditList.isEmpty())
				    	{
							for (Object[] e: ccCreditList)
							{
								if (e[0].toString().equals(courseCategory))
								{									
									ctgCredit = Float.parseFloat(e[1].toString());
									obtCredit = Float.parseFloat(e[6].toString());
									rmgCredit = Float.parseFloat(e[7].toString());
									
									break;
								}
							}
							
							for (Object[] e: ccCreditList)
							{
								if (e[0].toString().equals("UE"))
								{
									ueRmgCredit = Float.parseFloat(e[7].toString());
									break;
								}
							}
							
							/*System.out.println("courseCategory: "+ courseCategory +" | catalogType: "+ catalogType 
									+" | ccCourseId: "+ ccCourseId +" | basketCategory: "+ basketCategory 
									+" | ccCredit: "+ ccCredit);
							System.out.println("ctgCredit: "+ ctgCredit +" | obtCredit: "+ obtCredit 
									+" | rmgCredit: "+ rmgCredit +" | ueRmgCredit: "+ ueRmgCredit);*/
							/*System.out.println("histCredit: "+ histCredit +" | pvsCredit: "+ pvsCredit 
									+" | cuRegCredit: "+ cuRegCredit +" | cuWlCredit: "+ cuWlCredit);*/
				    	}
												
						if (!ccCreditList.isEmpty())
				    	{
							psRegList.clear();
							psRegList = compulsoryCourseConditionDetailService.getByCourseId(pSemesterSubId, 
											pProgramGroupId, pStudentStartYear, courseCode);
							if (!psRegList.isEmpty())
					    	{
								if (studCompulsoryCourse.contains(courseCode))
								{
									compCourseFlag = 2;
								}
								else
								{
									msg = "Selected course "+ courseCode +" is under compulsory course list and " 
											+"you are not eligible to take this course.";
								}
					    	}
							else
							{
								compCourseFlag = 3;
							}
				    	}
							
						if ((!ccCreditList.isEmpty()) && ((compCourseFlag == 2) || (compCourseFlag == 3)))
				    	{
							if (courseCategory.equals("PC") || courseCategory.equals("UC"))
							{
								if ((genericCoursetype.equals("PJT")) && (evaluationType.equals("CAPSTONE")))
								{
									rgrAllowFlag = 1;
								}
								else if (courseCategory.equals("UC") && catalogType.equals("BC"))
								{
									if (basketCategory.equals("LANGUAGE"))
									{
										bskObtCredit = programmeSpecializationCurriculumCreditService.getBasketCtgCreditByRegisterNo(
															pSemesterSubId, registerNumber2, ccCourseId);
										//System.out.println("bskObtCredit: "+ bskObtCredit);
										
										if ((bskObtCredit + courseCredit) <= ccCredit)
										{
											rgrAllowFlag = 1;
										}
										else if ((compCourseFlag == 3) && (optionNAStatus == 1) 
													&& ((bskObtCredit + courseCredit) > ccCredit) && (ueRmgCredit > 0))
										{
											RUCUEAllowFlag = 1;
										}
										else
										{
											if (optionNAStatus == 1)
											{
												msg = "You already completed the "+ basketCategory +" basket credit limit "+ ccCredit 
														+" under "+ courseCategory +" category and also the UE category credit limit.  "
														+"So, not allowed to register.";
											}
											else
											{
												msg = "You already completed the "+ basketCategory +" basket credit limit "+ ccCredit 
														+" under "+ courseCategory +" category.";
											}
										}
									}
									else if (((obtCredit + courseCredit) <= ctgCredit) && (rmgCredit > 0))
									{
										rgrAllowFlag = 1;
									}
									
									if ((compCourseFlag == 3) && (optionNAStatus == 1))
									{
										psRegList.clear();
										psRegList = studentHistoryService.getStudentHistoryCS2(registerNumber2, courseCode, 
														studStudySystem, pProgramSpecId, pStudentStartYear, pCurriculumVersion, 
														pSemesterSubId, courseCategory, courseOption, ccCourseId);
										if (!psRegList.isEmpty())
										{
											csAllowFlag = 1;
										}
									}
									
									if ((rgrAllowFlag == 2) && (csAllowFlag == 2) && (RUCUEAllowFlag == 2) && (!msg.equals("")))
									{
										msg = "Selected course "+ courseCode +" credit is exceeding over "+ ctgCredit 
												+" credit under "+ courseCategory +" category (i.e. Result Published: "
												+ obtCredit +" | Course Credit: "+ courseCredit +").  So, not allowed to register.";
									}
								}				
								else if (((obtCredit + courseCredit) <= ctgCredit) && (rmgCredit > 0))
								{
									rgrAllowFlag = 1;
								}
								else
								{
									if (rmgCredit <= 0)
									{
										msg = "You already completed or registered "+ ctgCredit +" credit under "
												+ courseCategory +" category.  So, not allowed to register.";
									}
									else
									{
										msg = "Selected course "+ courseCode +" credit is exceeding over "
												+ ctgCredit +" credit under "+ courseCategory +" category "
												+"(i.e. Result Published: "+ obtCredit +" | Course Credit: "
												+ courseCredit +").  So, not allowed to register.";
									}
								}
							}
							else if (courseCategory.equals("PE"))
							{
								if ((rmgCredit > 0) || studCompulsoryCourse.contains(courseCode))
								{
									rgrAllowFlag = 1;
								}
								else
								{	
									//Honors Eligibility Check 
									if ((compCourseFlag == 3) && (optionNAStatus == 1) && (cgpa >= 8))
									{	
										psRegList.clear();
										psRegList = semesterMasterService.getAdditionalLearningTitleByLearnTypeGroupIdSpecIdAndCourseCode(
														1, "HON", pProgramGroupId, pProgramSpecId, courseCode, studStudySystem);
										if (!psRegList.isEmpty())
										{
											honAllowFlag = 1;
											
											/*shmList2.clear();
											shmList2 = programmeSpecializationCurriculumCreditService.getStudentHonourEligibleDetailByRegisterNo(
															pProgramSpecId, pStudentStartYear, pCurriculumVersion, registerNumber2);
											if (!shmList2.isEmpty())
										   	{
												for (Object[] e: shmList2)
												{
													//honEarnCredit = Integer.parseInt(e[0].toString());
													honEarnCredit = Float.parseFloat(e[0].toString());
													honCompCount = Integer.parseInt(e[1].toString());
													break;
												}
										   	}
											//System.out.println("honEarnCredit: "+ honEarnCredit +" | honCompCount: "+ honCompCount);
																				
											if (catalogType.equals("CC") && (pStudentStartYear <= 2018) 
													&& (honEarnCredit >= 15) && (honCompCount >= 1))
											{
												honAllowFlag = 1;
										    }
											else if (catalogType.equals("CC") && (pStudentStartYear >= 2019) 
														&& (honEarnCredit >= 20) && (honCompCount >= 1))
											{											
												honAllowFlag = 1;
										    }*/
										}
									}
									
									//Audit Eligibility Check
									if ((compCourseFlag == 3) && (eoAllowStatus == 1) && (cgpa >= 8) && (audCount < 1))
									{
										audAllowFlag = 1;
									}
									
									//Additional Eligibility Check
									if ((compCourseFlag == 3) && (peAdlAllowStatus == 1) && (adlCount < 1))
									{
										adlAllowFlag = 1;
									}
									
									//RPEUE Eligibility Check
									if ((compCourseFlag == 3) && (rmgCredit <= 0) && (ueRmgCredit > 0))
									{
										RPEUEAllowFlag = 1;
									}
								}
								
								//Course Substitution Eligibility Check
								if ((compCourseFlag == 3) && (optionNAStatus == 1))
								{
									psRegList.clear();
									psRegList = studentHistoryService.getStudentHistoryCS2(registerNumber2, courseCode, 
													studStudySystem, pProgramSpecId, pStudentStartYear, pCurriculumVersion, 
													pSemesterSubId, courseCategory, courseOption, ccCourseId);
									if (!psRegList.isEmpty())
									{
										csAllowFlag = 1;
									}
								}
							}
							else if (courseCategory.equals("UE"))
							{
								if ((rmgCredit > 0) || studCompulsoryCourse.contains(courseCode))	
								{
									rgrAllowFlag = 1;
									flag10 = 1;
								}
								else
								{
									//Minors Eligibility Check
									if ((compCourseFlag == 3) && (optionNAStatus == 1) && (cgpa >= 8))
									{
										psRegList.clear();
										psRegList = semesterMasterService.getAdditionalLearningTitleByLearnTypeGroupIdSpecIdAndCourseCode(
														1, "MIN", pProgramGroupId, pProgramSpecId, courseCode, studStudySystem);
										if (!psRegList.isEmpty())
								    	{
											minAllowFlag = 1;
								    	}
									}
									
									//Audit or Additional Eligibility Check
									if ((compCourseFlag == 3) && (eoAllowStatus == 1))
									{
										if ((cgpa >= 8) && (audCount < 1))
										{
											audAllowFlag = 1;
										}
										
										if (adlCount < 1)
										{
											adlAllowFlag = 1;
										}
									}
								}
								
								//Course Substitution Eligibility Check
								if ((compCourseFlag == 3) && (optionNAStatus == 1))
								{
									psRegList.clear();
									psRegList = studentHistoryService.getStudentHistoryCS2(registerNumber2, courseCode, 
													studStudySystem, pProgramSpecId, pStudentStartYear, pCurriculumVersion, 
													pSemesterSubId, courseCategory, courseOption, ccCourseId);
									if (!psRegList.isEmpty())
									{
										csAllowFlag = 1;
									}
								}
							}
							else if (courseCategory.equals("BC"))
							{								
								msg = "Bridge Course category as regular (i.e. fresh course) are not allowed for registration.";
							}
							else if (ctgCredit > 0)
							{
								if (rmgCredit > 0)	
								{
									rgrAllowFlag = 1;
								}
							}
							else
							{
								rgrAllowFlag = 1;
							}
				    	}
						else if ((ccCreditList.isEmpty()) && ((compCourseFlag == 2) || (compCourseFlag == 3)))
						{
							msg = "Curriculum credit distribution detail is not available for your specialization. "
									+"So, not allowed to register.";
						}
												
						if ((rgrAllowFlag == 1) || (csAllowFlag == 1) || (honAllowFlag == 1) 
								|| (minAllowFlag == 1) || (RUCUEAllowFlag == 1) || (RPEUEAllowFlag == 1) 
								|| (adlAllowFlag == 1) || (audAllowFlag == 1))
						{
							flag10 = 1;
						}
						else if ((msg == null) || msg.equals(""))
						{
							msg = "You already completed the curriculum credit limit or you may not eligible to provide other "
									+"options.  So not allowed to Register.";
						}
					}
				}
				else
				{
					flag10 = 1;
				}
			}
						
			if ((flag == 1) && (flag2 == 1) && (flag3 == 1) && (flag4 == 1) && (flag5 == 1) 
					&& (flag6 == 1) && (flag7 == 1) && (flag8 == 1) && (flag9 == 1) && (flag10 == 1))
			{					
				//To assign the course equivalence registration detail value
				if ((courseOption.equals("RR")) || (courseOption.equals("RRCE")) 
						|| (courseOption.equals("GI")) || (courseOption.equals("GICE")) 
						|| (courseOption.equals("RGCE")) || (courseOption.equals("RWCE")) 
						|| (courseOption.equals("RPCE")))
				{ 
					subCourseOption = historyCourseId;
					subCourseDate = historyExamMonth;
										
					if (courseType.equals("NONE"))
					{
						subCrTypeList = semesterMasterService.getCourseTypeComponentByGenericType(historyGenericCourseType);
						crTpCount = semesterMasterService.getCourseTypeComponentByGenericType(genericCoursetype).size();
					}
					else
					{
						subCrTypeList = historyCourseTypeList;
					}
					
					for (String e: subCrTypeList)
					{
						subCrType = "";
						if ((genericCoursetype.equals("ETLP") || genericCoursetype.equals("ETL")
								|| genericCoursetype.equals("ETP")) && (e.equals("ETH")))
						{
							subCrType = "ETH";
						}
						else if ((genericCoursetype.equals("ETLP") || genericCoursetype.equals("ETL") 
									|| genericCoursetype.equals("ETP")) && (e.equals("TH")))
						{
							subCrType = "TH";
						}
						else if ((genericCoursetype.equals("ETLP") || genericCoursetype.equals("ETL") 
									|| genericCoursetype.equals("ELP")) && (e.equals("ELA")))
						{
							subCrType = "ELA";
						}
						else if ((genericCoursetype.equals("ETLP") || genericCoursetype.equals("ETL") 
									|| genericCoursetype.equals("ELP")) && (e.equals("LO")))
						{
							subCrType = "LO";
						}
						else if ((genericCoursetype.equals("ETLP") || genericCoursetype.equals("ETP") 
									|| genericCoursetype.equals("ELP")) && (e.equals("EPJ")))
						{
							subCrType = "EPJ";
						}
						else if ((genericCoursetype.equals("TH")) && (e.equals("ETH")))
						{
							subCrType = "ETH";
						}
						else if ((genericCoursetype.equals("LO")) && (e.equals("ELA")))
						{
							subCrType = "ELA";
						}
						else if ((genericCoursetype.equals("PJT")) && (e.equals("EPJ")))
						{
							subCrType = "EPJ";
						}
						else if (genericCoursetype.equals(e))
						{
							subCrType = e;
						}
						else
						{
							subCrType = historyGenericCourseType;
						}
						
						if (subCourseType.equals("NONE"))
						{
							subCourseType = subCrType;
						}
						else
						{
							subCourseType = subCourseType +","+ subCrType;
						}
						
						subCrCount++;
						if (subCrCount >= crTpCount)
						{
							break;
						}
					}
					
					//If history course component type is less than registered component type
					/*System.out.println("rrAllowFlag: "+ rrAllowFlag +" | subCrCount: "+ subCrCount +
							" | crTpCount: "+ crTpCount);*/
					if ((rrAllowFlag == 1) && (subCrCount != crTpCount) && (subCrCount == (crTpCount-1)))
					{
						subCourseType = subCourseType +",NONE";
					}
					else if ((rrAllowFlag == 1) && (subCrCount != crTpCount) && (subCrCount == (crTpCount-2)))
					{
						subCourseType = subCourseType +",NONE,NONE";
					}
				}
								
				regflag = 1;
				msg = "Success";
			}
		}
		catch (Exception ex)
		{
			logger.trace(ex);
		}
		
		//Generating the Authentication Key Value
		authKeyVal = generateCourseAuthKey(pRegisterNumber, pCourseId, regflag, 1);
				
		/*System.out.println("Flag: "+ flag +" | Flag2: "+ flag2 +" | Flag3: "+ flag3 +
		" | Flag4: "+ flag4 +" | Flag5: "+ flag5 +" | Flag6: "+ flag6 +" | Flag7: "+ flag7 +
		" | Flag8: "+ flag8 +" | Flag9: "+ flag9 +" | Flag10: "+ flag10);*/
				
		/*System.out.println("regflag: "+ regflag +" / msg: "+ msg 
				+" / courseOption: "+ courseOption +" / regAllowFlag: "+ regAllowFlag 
				+" / wlAllowFlag: "+ wlAllowFlag +" / ceCourseId: "+ ceCourseId 
				+" / courseType: "+ courseType +" / subCourseOption: "+ subCourseOption 
				+" / audAllowFlag: "+ audAllowFlag +" / subCourseType: "+ subCourseType 
				+" / subCourseDate: "+ subCourseDate +" / rgrAllowFlag: "+ rgrAllowFlag 
				+" / honAllowFlag: "+ honAllowFlag +" / adlAllowFlag: "+ adlAllowFlag 
				+" / authKeyVal: "+ authKeyVal +" / RPEUEAllowFlag: "+ RPEUEAllowFlag 
				+" / csAllowFlag: "+ csAllowFlag +" / RUCUEAllowFlag: "+ RUCUEAllowFlag);*/
			
		return regflag +"/"+ msg +"/"+ courseOption +"/"+ regAllowFlag +"/"+ wlAllowFlag 
					+"/"+ ceCourseId +"/"+ courseType +"/"+ subCourseOption +"/"+ audAllowFlag 
					+"/"+ subCourseType +"/"+ subCourseDate +"/"+ rgrAllowFlag +"/"+ honAllowFlag 
					+"/"+ minAllowFlag +"/"+ courseCategory +"/"+ adlAllowFlag +"/"+ authKeyVal 
					+"/"+ RPEUEAllowFlag +"/"+ csAllowFlag +"/"+ RUCUEAllowFlag +"/"+ ccCourseId;
	}

	
	//Checking slot clash
	public String checkClash(Integer patternId, List<String> clashSlotList, String pSemesterSubId, String pRegisterNumber, 
						String pRegType, String pOldClassId)
	{
		int clashStatus = 2;
		String message = "NONE", slot = "";
		String[] clashStatusArray = new String[]{};
		
		List<String> tempStringList = new ArrayList<String>();
		List<Object[]> objectList = new ArrayList<Object[]>();
		Map<String, List<SlotTimeMasterModel>> slotTimeMapList = new HashMap<String, List<SlotTimeMasterModel>>();
		
		//System.out.println("patternId: "+ patternId +" | clashSlotList: "+ clashSlotList 
		//		+" | pSemesterSubId: "+ pSemesterSubId +" | pRegisterNumber: "+ pRegisterNumber 
		//		+" | pRegType: "+ pRegType +" | pOldClassId: "+ pOldClassId);
		
		if (clashSlotList.isEmpty())
		{
			clashStatus = 1;
		}
		else if ((!clashSlotList.isEmpty()) && (pSemesterSubId != null) && (pRegisterNumber != null))
		{
			//Get the Slot Time Master By Semester
			slotTimeMapList = semesterMasterService.getSlotTimeMasterCommonTimeSlotBySemesterSubIdAsMap(Arrays.asList(pSemesterSubId));
			
			//Get list of Registration Slots based on Adding or Modifying the Course
			if ((pRegType.equals("MODIFY")) && (!pOldClassId.equals("")) && (!pOldClassId.equals(null)))
			{
				objectList = courseRegistrationService.getRegisteredSlotsforUpdate2(pSemesterSubId, pRegisterNumber, 
									pOldClassId);
			}
			else
			{
				objectList = courseRegistrationService.getRegisteredSlots2(pSemesterSubId, pRegisterNumber);
			}
			
			//Checking the clash with Registered Slot
			if (!objectList.isEmpty())
			{
				//System.out.println("Checking clash with registered Slots==>");
				for (String clhslt : clashSlotList)
				{
					clashStatus = 2;
					clashStatusArray = new String[]{};
					
					clashStatusArray = courseAllocationService.getClashStatus(patternId, clhslt, objectList, slotTimeMapList).split("\\|");
					clashStatus = Integer.parseInt(clashStatusArray[0].toString());
					slot = clashStatusArray[1].toString();
	    			
	    			if (clashStatus == 2)
	    			{
	    				message = "Selected slot clashed with "+ slot +" slot or combination of "+ slot +" slots in Registered Course(s).";
	    				break;
	    			}
	    			else
					{
						clashStatus = 1;
					}
				}
			}
			else
			{
				clashStatus = 1;
			}
			
			//Getting Waiting List Slots & Checking Clash
			if (clashStatus == 1)
			{
				clashStatus = 2;
				
				objectList.clear();
				objectList = courseRegistrationWaitingService.getWaitingSlots2(pSemesterSubId, pRegisterNumber);
				if (!objectList.isEmpty())
				{
					//System.out.println("Checking clash with Waiting Slots==>");
					for (String clhslt : clashSlotList)
					{
						clashStatus = 2;
						clashStatusArray = new String[]{};
						
						clashStatusArray = courseAllocationService.getClashStatus(patternId, clhslt, objectList, slotTimeMapList).split("\\|");
						clashStatus = Integer.parseInt(clashStatusArray[0].toString());
						slot = clashStatusArray[1].toString();
		    			
		    			if (clashStatus == 2)
		    			{
		    				message = "Selected slot clashed with "+ slot +" slot or combination of "+ slot +" slots in Waitlist Course(s).";
		    				break;
		    			}
		    			else
						{
							clashStatus = 1;
						}
					}
				}
				else
				{
					clashStatus = 1;
				}
			}
						
			//Checking the selected slot for Embedded Course (i.e. ETH/ELA) & whether it is clashed with each other
			if (clashStatus == 1)
			{
				clashStatus = 2;
				
				if (clashSlotList.size() >= 2)
				{
					//System.out.println("Checking clash Theory Slot with Lab Slot==>");
					tempStringList.clear();
					tempStringList.addAll(Arrays.asList(clashSlotList.get(1).split("/")));
					if (!tempStringList.isEmpty())
					{
						for (String clhst : clashSlotList.get(0).split("/"))
						{
							clashStatus = 2;
														
							if (tempStringList.contains(clhst))
							{
								message = "Selected theory slot clashed with selected lab slot";
								break;
							}
							else
							{
								clashStatus = 1;
							}	
						}
					}
				}
				else
				{
					clashStatus = 1;
				}
			}	
		}
		//System.out.println("Return=> clashStatus: "+ clashStatus +" | message: "+ message);
		
		return clashStatus +"/"+ message;
	}
	
	
	//Checking the compulsory course is registered or not & then return the status  
	public int compulsoryCourseCheck(Integer progGroupId, Integer studentBatch, Integer studentGradYear,
					Integer semesterId, String semesterSubId, String registerNumber, String[] classGroupId,
					String[] classType, String progSpecCode, Integer progSpecId, String progGroupCode, 
					String oldRegisterNumber, List<String> compulsoryCourse, String costCentreCode)//M1
	{
		int returnStatus = 2;
		String compCourseStatus = "NONE";
		
		compCourseStatus = compulsoryCoursePriorityCheck(progGroupId, studentBatch, studentGradYear, semesterId, 
								semesterSubId, registerNumber, classGroupId, classType, progSpecCode, progSpecId, 
								progGroupCode, oldRegisterNumber, compulsoryCourse, costCentreCode, "");
		if (!compCourseStatus.equals("NONE"))
		{
			returnStatus = 1;
		}
		//System.out.println("compCourseStatus: "+ compCourseStatus +" | returnStatus: "+ returnStatus);
		
		return returnStatus;
	}
	
	//Checking the compulsory course is registered as per priority
	public String compulsoryCoursePriorityCheck(Integer progGroupId, Integer studentBatch, Integer studentGradYear,
						Integer semesterId, String semesterSubId, String registerNumber, String[] classGroupId,
						String[] classType, String progSpecCode, Integer progSpecId, String progGroupCode, 
						String oldRegisterNumber, List<String> compulsoryCourse, String costCentreCode, 
						String regCourseCode)//M2
	{
		String compCourseStatus = "NONE";
		int checkCompFlag = 2, checkCompFlag2 = 2;
		
		List<CourseRegistrationModel> courseRegistrationList = new ArrayList<CourseRegistrationModel>();
		List<CourseRegistrationWaitingModel> courseRegistrationWaitList = new ArrayList<CourseRegistrationWaitingModel>();
		List<CourseAllocationModel> courseAllocationCrsAvbList = new ArrayList<CourseAllocationModel>();
		
		//System.out.println("compulsoryCourse: "+ compulsoryCourse);
		if (!compulsoryCourse.isEmpty())
		{
			for (String crsId : compulsoryCourse)
			{
				courseRegistrationList.clear();
				courseRegistrationWaitList.clear();
				courseAllocationCrsAvbList.clear();
				checkCompFlag = 2;
				checkCompFlag2 = 2;
				
				//Checking in the Registration & Waiting
				courseRegistrationList = courseRegistrationService.getByRegisterNumberCourseCode(semesterSubId,
											registerNumber, crsId);
				if (courseRegistrationList.isEmpty())
				{
					courseRegistrationWaitList = courseRegistrationWaitingService.getByRegisterNumberAndCourseCode(
														semesterSubId, registerNumber, crsId);
					if (courseRegistrationWaitList.isEmpty())
					{
						checkCompFlag = 1;
					}
				}
				
				//Checking in Course Allocation whether Seat is available or not
				if (checkCompFlag == 1)
				{
					courseAllocationCrsAvbList = courseAllocationService.getCourseAllocationCourseCodeAvbList(semesterSubId, 
													classGroupId, classType, crsId, progGroupCode, progSpecCode, costCentreCode);
					if (!courseAllocationCrsAvbList.isEmpty())
					{
						checkCompFlag2 = 1;
					}
				}

				if ((checkCompFlag == 1) && (checkCompFlag2 == 1))
				{
					if (crsId.equals(regCourseCode))
					{
						compCourseStatus = "SUCCESS";
					}
					else
					{
						compCourseStatus = "Kindly register the compulsory course "+ crsId +" as per priority.";
					}
					break;
				}
			}
		}
		//System.out.println("compCourseStatus: "+ compCourseStatus);
		
		return compCourseStatus;
	}	
	
		
	//Checking the Soft Skill course
	public List<String> SoftSkillCourseCheck(Integer progGroupId, Integer studentBatch, Integer studentGradYear,
							String registerNumber, String progSpecCode, String progGroupCode, String semesterSubId)
	{
		List<String> ssCourseList = new ArrayList<String>();
		
		ssCourseList = compulsoryCourseConditionDetailService.getSoftSkillCourseList(semesterSubId, progGroupId, 
							studentBatch);
		if (ssCourseList.isEmpty())
		{
			ssCourseList.add("NIL");
		}

		return ssCourseList;
	}
	
	public Integer findStudentSemester(String progGroupCode, Integer studentBatch)
	{
		Integer tempStudentSemester = 0;
		
		if (progGroupCode.equals("MBA") || progGroupCode.equals("MIB"))
		{
			if (studentBatch == 2020)
			{
				tempStudentSemester = 3;
			} 
			else if (studentBatch == 2019)
			{
				tempStudentSemester = 6;
			}
			else if (studentBatch <= 2018)
			{
				tempStudentSemester = 7;
			}
		}
		else
		{
			if (studentBatch == 2020)
			{
				tempStudentSemester = 2;
			} 
			else if (studentBatch == 2019)
			{
				tempStudentSemester = 4;
			}
			else if (studentBatch == 2018)
			{
				tempStudentSemester = 6;
			}
			else if (studentBatch == 2017)
			{
				tempStudentSemester = 8;
			}
			else if (studentBatch == 2016)
			{
				tempStudentSemester = 10;
			}
			else if (studentBatch <= 2015)
			{
				tempStudentSemester = 11;
			}
		}
						
		return tempStudentSemester;
	}

	
	//checking the Active Time
	public int ActivePresentDateTimeCheck(String registerNo)
	{
		int activeStatus = 1, logStatus = 0;
		long timeDiff = 0;
		List<Object[]> regLogList = new ArrayList<Object[]>();
				
		regLogList = registrationLogService.getRegistrationLogTimeDifference(registerNo);
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
			
			if ((logStatus == 1) && (timeDiff <= 500))
			{
				activeStatus = 2;
			}
		}
				
		return activeStatus;
	}
	
	
	//Checking the Add or Drop Time
	public String AddorDropDateTimeCheck(Date startDate, Date endDate, String startTime, String endTime,
						String registerNo, int updateStatus, String ipAddress)
	{	
		int timeCheckFlag = 2;
		String timeCheckMessage = "NONE", presentDateTime = "", presentTime = "";
		Long startTimeVal = 0L, endTimeVal = 0L, presentTimeVal = 0L;
		
		Date presentDate = null;
		DateFormat format = new SimpleDateFormat("dd-MMM-yyyy");
		
		try
		{
			if ((startDate != null) && (endDate != null) && (startTime != null) && (!startTime.equals("")) 
					&& (endTime != null) && (!endTime.equals("")) && (registerNo != null) && (!registerNo.equals("")) 
					&& (ipAddress != null) && (!ipAddress.equals("")))
			{
				startTimeVal = Long.parseLong(startTime.replace(":", ""));
				endTimeVal = Long.parseLong(endTime.replace(":", ""));
				
				presentDateTime = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(new Date());					
				String[] presentDateTimeArr = presentDateTime.split(" ");
				presentDate = format.parse(presentDateTimeArr[0]);
				presentTime = presentDateTimeArr[1];
				presentTimeVal = Long.parseLong(presentTime.replace(":", ""));
				
				/*System.out.println("StartDate: "+ startDate +" | StartTime: "+ startTime 
											+" | Start Time Value: " + startTimeVal);
				System.out.println("EndDate: "+ endDate +" | EndTime: "+ endTime +" | EndTimeValue: " + endTimeVal);
				System.out.println("PresentDate: "+ presentDate +" | PresentTime: "+ presentTime
						 				+" | PresentTimeValue: " + presentTimeVal);*/
				
				//Based on Registration Schedule
				/*if ((presentDate.equals(startDate)) && (presentTimeVal >= startTimeVal)
							&& (presentTimeVal <= endTimeVal))
				{
					timeCheckMessage = "Success.";
					timeCheckFlag = 1;
				}
				else
				{
					if (presentDate.compareTo(startDate) < 0)
					{
						timeCheckMessage = "Your registration date and time duration "+ 
													new SimpleDateFormat("dd-MMM-yyyy").format(startDate)  
													+" between "+ startTime +" Hrs and "+ endTime +" Hrs.";
					}
					else if	((presentDate.equals(startDate)) && (presentTimeVal < startTimeVal))
					{
						timeCheckMessage = "Your registration time starts at "+ startTime +" Hrs and "
											+"ends at "+ endTime +" Hrs.";
					}
					else
					{
						timeCheckMessage = "Your registration time was over.  The duration was "+ 
												new SimpleDateFormat("dd-MMM-yyyy").format(startDate)  
												+" between "+ startTime +" Hrs and "+ endTime +" Hrs.";
					}
				}*/
				
				//Based on fixed Date & Time
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
					registrationLogService.UpdateActiveTimeStamp(registerNo);
				}
			}
			else
			{
				timeCheckMessage = "Session timed out.  Kindly logout and login again.";
			}
		}
		catch (Exception ex)
		{
			//System.out.println(ex);
		}

		return timeCheckFlag +"/"+ timeCheckMessage;
	}
	
	
	/*public final void callCaptcha(HttpServletRequest request, HttpServletResponse response, HttpSession session, 
							Model model) throws ServletException, IOException
	{
		Sdc_common_functions sdf = new Sdc_common_functions();
		sdf.doGet(request, response);
		String res = (String) session.getAttribute("ENCDATA");
		model.addAttribute("res1", res);
	}*/
	public void callCaptcha(HttpServletRequest request, HttpServletResponse response, HttpSession session, 
					Model model) throws ServletException, IOException
    {
		int num = 0;
        String randomChars = "", res = "";
        Sdc_common_functions sdf = new Sdc_common_functions();
        
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Max-Age", 0);
        response.setContentType("image/jpeg");
        
        randomChars = sdf.getWord();

        captchaManager.setHeight(40);
        captchaManager.setWidth(200);
        captchaManager.setForegroundColor(Color.BLUE);
        captchaManager.setBackgroundColor(new Color(249, 231, 159));
        
        num = new Random().nextInt(10);
        //System.out.println("num: "+ num);
        if(num<=5)
        {
        	captchaManager.setGridLines(true);
        	captchaManager.setDrawLines(false);
        }
        else
        {
        	captchaManager.setGridLines(false);
        	captchaManager.setDrawLines(true);
        }
        res = captchaManager.createCaptcha(randomChars);
        
        session.setAttribute("CAPTCHA", randomChars);
        session.setAttribute("ENCDATA", res);
        model.addAttribute("res1", res);
    }
	
	
	public String callClassGroup(Integer pSemesterId, String pProgramGroupCode, String costCentreCode, int academicYear, 
						int academicGraduateYear, int studentAdmissionYear, int studentGraduateYear) 
	{
		String returnClassGroup = "";
		
		
		if ((pSemesterId == 4) || (pSemesterId == 8) || (pSemesterId == 9))
		{
			if (pProgramGroupCode.equals("MBA") || pProgramGroupCode.equals("MBA5"))
			{
				returnClassGroup = "MBA";
			}
			else if (pProgramGroupCode.equals("RP") && costCentreCode.equals("VITBS"))
			{
				returnClassGroup = "MBA";
			}
			else
			{
				returnClassGroup = "ALL";
			}
		}
		else if ((pSemesterId == 7))
		{	
			returnClassGroup = "ST004";
			/*if (pProgramGroupCode.equals("MBA") || pProgramGroupCode.equals("MBA5"))
			{
				returnClassGroup = "ST003";
			}
			else if (pProgramGroupCode.equals("RP") && costCentreCode.equals("VITBS"))
			{
				returnClassGroup = "ST003";
			}
			else if (studentGraduateYear <= academicGraduateYear)
			{
				returnClassGroup = "ST002/ST003";
			}
			else
			{
				if (pProgramGroupCode.equals("RP"))
				{
					returnClassGroup = "ST002";
				}
				else if ((pProgramGroupCode.equals("MTECH") || pProgramGroupCode.equals("MCA")) 
								&& (studentGraduateYear == (academicGraduateYear+1)))
				{
					returnClassGroup = "ST002";
				}
				else if (pProgramGroupCode.equals("MTECH5") && (studentGraduateYear == (academicGraduateYear+1)))
				{
					//returnClassGroup = "ST002";
					returnClassGroup = "ST002/ST003";
				}
				else
				{
					returnClassGroup = "ST003";
				}
			}*/
		}
		else
		{
			returnClassGroup = "ALL";
		}
		
		return returnClassGroup;
	}
	
		
	//Generate Course Authorization Key
	public String generateCourseAuthKey(String registerNumber, String courseId, int validateStatus, int levelType)
	{
		String authKeyVal = "NONE";
		Sdc_common_functions scf = new Sdc_common_functions();
		
		if ((registerNumber == null) || registerNumber.equals(""))
		{
			registerNumber = "NONE";
		}
		
		if ((courseId == null) || courseId.equals(""))
		{
			courseId = "NONE";
		}
		
		if (levelType == 1)
		{
			authKeyVal = scf.Encrypt_String3(registerNumber +"_"+ courseId +"_"+ validateStatus +"_1", keyLength);
		}
		else if (levelType == 2)
		{
			authKeyVal = scf.Encrypt_String3(registerNumber +"_"+ courseId +"_"+ validateStatus +"_2", keyLength);
		}
		
		return authKeyVal;
	}
	
	//Validate the Course Authorization Key
	public int validateCourseAuthKey(String authorizationKey, String registerNumber, String courseId, int levelType)
	{
		int regValidFlag = 2;
				
		if ((authorizationKey != null) && (!authorizationKey.equals("")) 
				&& (!authorizationKey.equals("NONE")) && (registerNumber != null) 
				&& (!registerNumber.equals("")) && (!registerNumber.equals("NONE")) 
				&& (courseId != null) && (!courseId.equals("")) && (!courseId.equals("NONE")))
		{
			Sdc_common_functions scf3 = new Sdc_common_functions();
			
			if (levelType == 1)
			{
				regValidFlag = scf3.String_Validate3(registerNumber +"_"+ courseId +"_1_1", authorizationKey, keyLength);
			}
			else if (levelType == 2)
			{
				regValidFlag = scf3.String_Validate3(registerNumber +"_"+ courseId +"_1_2", authorizationKey, keyLength);
			}
		}
				
		return regValidFlag;
	}
		
	//Generate Page Authorization Key
	public String generatePageAuthKey(String registerNumber, int levelType)
	{
		String authKeyValue = "NONE";
		Sdc_common_functions scf = new Sdc_common_functions();
		
		if ((registerNumber == null) || registerNumber.equals(""))
		{
			registerNumber = "NONE";
		}
		
		if (levelType == 1)
		{
			authKeyValue = scf.Encrypt_String3(registerNumber +"_1", keyLength);
		}
		else if (levelType == 2)
		{
			authKeyValue = scf.Encrypt_String3(registerNumber +"_1_1", keyLength);
		}
		
		return authKeyValue;
	}
	
	//Validate Page Authorization Key
	public int validatePageAuthKey(String authorizationKey, String registerNumber, int levelType)
	{
		int regValidFlag = 2;
		
		if ((authorizationKey != null) && (!authorizationKey.equals("")) 
				&& (!authorizationKey.equals("NONE")) && (registerNumber != null) 
				&& (!registerNumber.equals("")) && (!registerNumber.equals("NONE")))
		{	
			Sdc_common_functions scf3 = new Sdc_common_functions();
			
			if (levelType == 1)
			{
				regValidFlag = scf3.String_Validate3(registerNumber +"_1", authorizationKey, keyLength);
			}
			else if (levelType == 2)
			{
				regValidFlag = scf3.String_Validate3(registerNumber +"_1_1", authorizationKey, keyLength);
			}
		}
				
		return regValidFlag;
	}
	
	
	//Registration Status
	public Integer getRegistrationStatus(Integer approvalStatus, String courseOption, String genericCourseType, 
						String evaluationType, String studentCategory)
	{
		Integer tempRegistrationStatus = 0;
		/*System.out.println("approvalStatus: "+ approvalStatus +" | courseOption: "+ courseOption 
					+" | genericCourseType: "+ genericCourseType + " | evaluationType: "+ evaluationType 
					+" | studentCategory: "+ studentCategory);*/
		
		if (approvalStatus == 1)
		{
			if (courseOption.equals("RGR") || courseOption.equals("AUD") 
					|| courseOption.equals("RGCE") || courseOption.equals("RPEUE") 
					|| courseOption.equals("RUCUE") || courseOption.equals("RGVC"))
			{
				if ((genericCourseType.equals("PJT")) && 
						(evaluationType.equals("CAPSTONE") || evaluationType.equals("GUIDE")))
				{
					tempRegistrationStatus = 7;
				}
				else
				{
					tempRegistrationStatus = 10;
				}
			}
			else
			{
				if (genericCourseType.equals("PJT") && evaluationType.equals("CAPSTONE"))
				{
					tempRegistrationStatus = 0;
				}
				else
				{
					if (studentCategory.equals("STARS"))
					{
						tempRegistrationStatus = 14;
					}
					else
					{
						tempRegistrationStatus = 1;
					}
				}
			}
		}
		
		return tempRegistrationStatus;
	}
	
	public String checkRegistrationDeleteCondition(String pSemesterSubId, String pRegisterNumber, String pCourseId, 
						Integer pProgramGroupId, String pProgramGroupCode, String programGroupMode, String pProgramSpecCode, 
						Integer pProgramSpecId, Integer pStudentStartYear, Float pCurriculumVersion, List<String> compulsoryCourse)
	{
		int deleteAllowFlag = 2;
		int checkFlag = 2, checkFlag2 = 2, checkFlag3 = 2, checkFlag4 = 2;
		
		String message = "", authKeyValue = "NONE";
		String courseCode = "", courseTitle = "", courseCategory = "NONE", courseOption = "NONE";
		
		CourseCatalogModel ccm = new CourseCatalogModel();
		List<Object[]> psRegList = new ArrayList<Object[]>();
		List<String> courseList = new ArrayList<String>();
		
		try
		{
			//Checking the select course is valid or not
			if ((pCourseId != null) && (!pCourseId.equals("")))
			{
				ccm = courseCatalogService.getOne(pCourseId);
				if (ccm != null)
				{
					courseCode = ccm.getCode();
					courseTitle = ccm.getTitle();
														
					//To get course category from curriculum
					if (pCurriculumVersion > 0)
					{
						psRegList.clear();
						psRegList = programmeSpecializationCurriculumDetailService.getCurriculumByAdmsnYearCCVersionAndCourseCode(
										pProgramSpecId, pStudentStartYear, pCurriculumVersion, courseCode);
						if (!psRegList.isEmpty())
				    	{
							for (Object[] e: psRegList)
							{
								courseCategory = e[0].toString();							
								break;
							}
				    	}
						else
						{
							courseCategory = "UE";
						}
					}
														
					checkFlag = 1;
				}
				else
				{
					message = "Invalid course code.";
				}
			}
			else
			{
				message = "Invalid course code";
			}
			
			//Checking whether student is already registered or not
			if (checkFlag == 1)
			{
				psRegList.clear();
				psRegList = courseRegistrationService.getCourseOptionByRegisterNumberAndCourseCode(pSemesterSubId, 
								pRegisterNumber, courseCode);
				if (!psRegList.isEmpty())
				{
					for (Object[] e: psRegList)
					{
						courseOption = e[3].toString(); 
						break;
					}
					checkFlag2 = 1;
				}
							
				if (checkFlag2 == 2)
				{
					psRegList.clear();
					psRegList = courseRegistrationWaitingService.getCourseOptionByRegisterNumberAndCourseCode(pSemesterSubId, 
									pRegisterNumber, courseCode);
					if (!psRegList.isEmpty())
					{
						for (Object[] e: psRegList)
						{
							courseOption = e[3].toString(); 
							break;
						}
						checkFlag2 = 1;
					}
					else
					{
						message = "Selected course "+ courseCode +" - "+ courseTitle +" is does not exist to delete.";  
					}
				}
			}
			
			//Checking whether student is deleting the blocked course
			if (checkFlag2 == 1)
			{
				if (!compulsoryCourse.contains(courseCode))
				{
					checkFlag3 = 1;
				}
				else
				{
					checkFlag3 = 2;
					message = "Selected course "+ courseCode +" - "+ courseTitle +" does not allowed to delete.  "
								+"Because, compulsory course(s) are not permitted to delete.";
				}
				
				if (checkFlag3 == 1)
				{
					courseList.clear();
					courseList = courseRegistrationService.getBlockedCourseIdByRegisterNumberForDelete(pSemesterSubId, 
											pRegisterNumber);				
					if (!courseList.contains(pCourseId))
					{
						checkFlag3 = 1;
					}
					else
					{
						checkFlag3 = 2;
						message = "Selected course "+ courseCode +" - "+ courseTitle +" does not allowed to delete.  "
									+"Because, invoice generated course(s) are not permitted to delete.";
					}
				}
			}
			
			//Checking whether student is deleting the Regular Course with other Options 
			//(i.e. RPEUE/ RUCPE/ MINOR/ HONOUR) in same category
			if (checkFlag3 == 1)
			{
				if (courseCategory.equals("UC") || courseCategory.equals("PE") 
						|| courseCategory.equals("UE") || courseCategory.equals("BC") 
						|| courseCategory.equals("NC"))
				{
					if (courseOption.equals("RGR") || courseOption.equals("RGCE") 
								|| courseOption.equals("RGP") || courseOption.equals("RGW") 
								|| courseOption.equals("RPCE") || courseOption.equals("RWCE"))
					{
						if (courseCategory.equals("BC") || courseCategory.equals("NC"))
						{
							checkFlag4 = 2;
						}
						else
						{
							courseList.clear();
							courseList = courseRegistrationService.getUECourseByRegisterNumberAndCourseOption(pSemesterSubId, 
											pRegisterNumber, courseCategory);
							if (courseList.isEmpty())
							{
								checkFlag4 = 1;
							}
							else
							{
								checkFlag4 = 2;
							}
										
							if (checkFlag4 == 1)
							{
								courseList.clear();
								courseList = courseRegistrationWaitingService.getUECourseByRegisterNumberAndCourseOption(
												pSemesterSubId, pRegisterNumber, courseCategory);
								if (courseList.isEmpty())
								{
									checkFlag4 = 1;
								}
								else
								{
									checkFlag4 = 2;
								}
							}
						}
						
						if (checkFlag4 == 2)
						{
							message = "Selected course "+ courseCode +" - "+ courseTitle +" not allowed to delete.";
							if (courseCategory.equals("UC"))
							{
								message = message +"  Kindly delete the registered course under course option "
											+"Regular (UC as UE).";
							}
							else if (courseCategory.equals("PE"))
							{
								message = message +"  Kindly delete the registered course under course option "
											+"Regular (PE as UE)/ Regular (Additional)/ Honour.";
							}
							else if (courseCategory.equals("UE"))
							{
								message = message +"  Kindly delete the registered course under course option "
											+"Regular (UC as UE)/ Regular (PE as UE)/ Regular (Additional)/ Minor.";
							}
							else if (courseCategory.equals("BC") || courseCategory.equals("NC"))
							{
								message = message +".  Bridge and Non-Credit category regular course(s) are not "
											+"allowed to delete.";
							}
						}
					}
					else
					{
						checkFlag4 = 1;
					}
				}
				else
				{
					checkFlag4 = 1;
				}
			}
			
			if ((checkFlag == 1) && (checkFlag2 == 1) && (checkFlag3 == 1) && (checkFlag4 == 1))
			{
				deleteAllowFlag = 1;
			}
		}
		catch (Exception ex)
		{
			logger.trace(ex);
		}
		
		//Generating the Authorization key
		authKeyValue = generateCourseAuthKey(pRegisterNumber, pCourseId, deleteAllowFlag, 1);
		
		/*System.out.println("checkFlag: "+ checkFlag +" | checkFlag2: "+ checkFlag2 
				+" | checkFlag3: "+ checkFlag3 +" | checkFlag4: "+ checkFlag4);*/
		/*System.out.println("deleteAllowFlag: "+ deleteAllowFlag +" / message: "+ message 
				+" / authKeyValue: "+ authKeyValue);*/
				
		return deleteAllowFlag +"|"+ message +"|"+ authKeyValue;
	}
	
	//Checking the Registration Session Date & Time
	public String registrationSessionDateTimeCheck(Date startDate)
	{	
		String tempStatus = "NONE";
		
		Date presentDate = null;
		DateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
		long startTimeVal = 0, endTimeVal = 0, presentTimeVal = 0, maxTimeVal = 0;
				
		String presentDateTime = "", presentTime = "", endTime = "", allowStartTime = "", slotDate = null;
		String[] timeArr = new String[]{};
		List<String> slotTimeList = new ArrayList<String>();
		//String startTime = "";
						
		try
		{
			slotDate = df.format(startDate);
			//System.out.println("StartDate: "+ startDate +" | slotDate: "+ slotDate);
						
			slotTimeList.add("09:00:00|10:30:00|08:55:00|"+ slotDate);
			slotTimeList.add("11:00:00|12:30:00|10:55:00|"+ slotDate);
			slotTimeList.add("14:00:00|15:30:00|13:55:00|"+ slotDate);
			slotTimeList.add("16:00:00|17:30:00|15:55:00|"+ slotDate);
			slotTimeList.add("18:30:00|23:30:00|18:25:00|"+ slotDate);
									
			presentDateTime = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(new Date());					
			String[] presentDateTimeArr = presentDateTime.split(" ");
			presentDate = df.parse(presentDateTimeArr[0]);
			presentTime = presentDateTimeArr[1];
			presentTimeVal = Long.parseLong(presentTime.replace(":", ""));
			/*System.out.println("presentDate: "+ presentDate +" | presentTime: "+ presentTime 
					+" | presentTimeVal: "+ presentTimeVal);*/
			
			if (!slotTimeList.isEmpty())
			{
				for(String e : slotTimeList)
				{
					timeArr = e.split("\\|"); 
					//startTime = timeArr[0];
					endTime = timeArr[1];
					allowStartTime = timeArr[2];
					startDate = df.parse(timeArr[3]);
					
					startTimeVal = Long.parseLong(allowStartTime.replace(":", ""));
					endTimeVal = Long.parseLong(endTime.replace(":", ""));
					maxTimeVal = endTimeVal;
					//System.out.println("StartDate: "+ startDate +" | endTime: "+ endTime 
					//		+" | allowStartTime: "+ allowStartTime +" | startTimeVal: "+ startTimeVal
					//		+" | endTimeVal: "+ endTimeVal);
					
					if (presentDate.equals(startDate) 
							&& (presentTimeVal >= startTimeVal) && (presentTimeVal <= endTimeVal))
					{
						tempStatus = "SUCCESS";
						break;
					}
				}
			}	
			
			if (!tempStatus.equals("SUCCESS"))
			{
				if (presentDate.before(startDate))
				{		
					tempStatus = "Registration session not yet started.";
				}
				else if	(presentDate.equals(startDate) && (presentTimeVal <= maxTimeVal))
				{
					tempStatus = "Registration session not yet started.";
				}
				else if	(presentDate.equals(startDate) && (presentTimeVal > maxTimeVal))
				{
					tempStatus = "Registration closed.";
				}
				else
				{
					tempStatus = "Registration closed.";
				}
			}
		}
		catch (Exception ex)
		{
			//System.out.println(ex);
		}
		//System.out.println("tempStatus: "+ tempStatus);
		
		return tempStatus;
	}
	
	///Get the maximum credit
	public String getMinimumAndMaximumCreditLimit(String semesterSubId, String registerNumber, String programGroupCode, 
						String costCentreCode, int studentStartYear, int studentGraduateYear, int checkGraduateYear, 
						int semesterId, String programSpecializationCode)
	{
		int minCredit = 16, maxCredit = 27;
		
		if (programGroupCode.equals("MBA") || programGroupCode.equals("MBA5") 
				|| (programGroupCode.equals("RP") && costCentreCode.equals("VITBS")))
		{
			if (studentStartYear <= 2018)
			{
				minCredit = 14;
				maxCredit = 18;
			}
			else
			{
				minCredit = 20;
				maxCredit = 27;
			}
		}
		else if ((semesterId == 1) && (studentGraduateYear <= checkGraduateYear) 
					&& programGroupCode.equals("MTECH5") && programSpecializationCode.equals("MIS"))
		{
			if (courseRegistrationService.getProjectCourseCountByRegisterNumber(semesterSubId, registerNumber, 
					Arrays.asList("CAPSTONE","GUIDE")) > 0)
			{
				maxCredit = 30;
			}
			else
			{
				maxCredit = 27;
			}
		}
		else if ((semesterId == 5) && (studentGraduateYear <= checkGraduateYear))
		{
			if (courseRegistrationService.getProjectCourseCountByRegisterNumber(semesterSubId, registerNumber, 
					Arrays.asList("CAPSTONE")) > 0)
			{
				maxCredit = 30;
			}
			else
			{
				maxCredit = 27;
			}
		}
		
		return minCredit +"|"+ maxCredit;
	}
	
	//Validate Course & Sending the OTP
	public String validateCourseAndSendOTP(String semesterSubId, String registerNumber, String courseId, 
						String studentEMailId, String IpAddress, String processType)
	{	
		int validateStatus = 2, otpReasonType = 0, flag = 2, flag2 = 2, flag3 = 2, flag4 = 2;	
		String msg = "NONE", authKeyVal = "NONE", mobileOTP = "NONE", emailOTP = "NONE"; 		 
		String courseCode = "", semesterShortDesc = "", semesterDesc = "", mailSubject = "", 
					mailBody = "", mailStatus = "";
					
		CourseCatalogModel ccm = new CourseCatalogModel();
		SemesterDetailsModel sdm = new SemesterDetailsModel();
		List<Object[]> objectList = new ArrayList<Object[]>();
		
		/*System.out.println("semesterSubId: "+ semesterSubId +" | registerNumber: "+ registerNumber 
				+" | courseId: "+ courseId +" | studentEMailId: "+ studentEMailId +" | IpAddress: "+ IpAddress 
				+" | processType: "+ processType);*/
								
		try
		{
			//Checking the select course is valid or not
			if ((courseId != null) && (!courseId.equals("")))
			{
				ccm = courseCatalogService.getOne(courseId);
				if (ccm != null)
				{
					courseCode = ccm.getCode();					
					flag = 1;
				}
				else
				{
					msg = "Invalid course...!";
				}
			}
			else
			{
				msg = "Invalid course...!";
			}
						
			//Validating the Mobile No.
			if (flag == 1)
			{
				if((studentEMailId != null) && (!studentEMailId.equals("")) && (!studentEMailId.equals("NONE")))
				{
					flag2 = 1;
				}
				else
				{
					msg = "E-Mail Id is not properly updated.  Kindly check your profile and update it."; 
				}
			}
			
			//Validating the Process Type.
			if (flag2 == 1)
			{
				if (processType.equals("DELETE") || processType.equals("MODIFY"))
				{
					flag3 = 1;
				}
				else
				{
					msg = "Invalid process type.  Kindly proceed once again."; 
				}
			}
									
			//Sent & Check the Mail Status			
			if (flag3 == 1)
			{	
				//Semester Sub Id Details
				sdm = semesterMasterService.getSemesterDetailBySemesterSubId(semesterSubId);
				if(sdm != null)
				{
					semesterShortDesc = sdm.getDescriptionShort();
					semesterDesc = sdm.getDescription();
				}
				
				mobileOTP = getRandomOtp(5);
				emailOTP = getRandomOtp(5);
				if (emailOTP.equals(mobileOTP))
				{
					mobileOTP = getRandomOtp(5);
				}
				
				if (processType.equals("DELETE"))
				{					
					mailSubject = semesterShortDesc +" - Course Delete OTP";					
					mailBody = "<html><body><b>Dear Student ("+ registerNumber +"),</b><br><br>"+ System.lineSeparator();
					mailBody = mailBody +"OTP to delete the course "+ courseCode +" is <b>"+ emailOTP +"</b> for "
								+ semesterDesc +".";
					mailBody = mailBody +"<br><br>This is an auto generated mail.  Kindly don't reply or send message "
								+"to this mail id."+ System.lineSeparator() +"<br><br>Thanks !!</body></html>";
					otpReasonType = 2;
				}
				else if (processType.equals("MODIFY"))
				{
					mailSubject = semesterShortDesc +" - Course Modify OTP";
					mailBody = "<html><body><b>Dear Student ("+ registerNumber +"),</b><br><br>"+ System.lineSeparator();
					mailBody = mailBody +"OTP to modify the course "+ courseCode +" is <b>"+ emailOTP +"</b> for "
								+ semesterDesc +".";
					mailBody = mailBody +"<br><br>This is an auto generated mail.  Kindly don't reply or send message "
								+"to this mail id."+ System.lineSeparator() +"<br><br>Thanks !!</body></html>";
					otpReasonType = 3;
				}
				
				//Add or Update the OTP Data
				objectList = courseRegistrationWithdrawService.getCourseWithdrawOTP(semesterSubId, 
								registerNumber, courseId, otpReasonType);
				if (objectList.isEmpty())
				{
					courseRegistrationWithdrawService.addCourseWithdrawOTP(semesterSubId, registerNumber, 
							courseId, otpReasonType, emailOTP, mobileOTP, registerNumber, IpAddress);
				}
				else
				{
					courseRegistrationWithdrawService.modifyCourseWithdrawOTP(semesterSubId, registerNumber, 
							courseId, otpReasonType, emailOTP, mobileOTP, registerNumber, IpAddress);
				}
		
				//Sending E-Mail
				mailStatus = MailUtility.triggerMail(mailSubject, mailBody, "", studentEMailId);
				if (mailStatus.length() > 250)
				{
					mailStatus = mailStatus.substring(0, 250);
				}
				
				//Update the Mail Response Status
				courseRegistrationWithdrawService.modifyCourseWithdrawOTPResponse(semesterSubId, registerNumber, 
						courseId, otpReasonType, mailStatus);
				
				if (mailStatus.equals("SUCCESS"))
				{
					flag4 = 1;
				}
				else
				{
					msg = "Not a valid E-Mail Id.  Kindly check your profile and update it."; 
				}
			}
			
			//System.out.println("Flag: "+ flag +" | Flag2: "+ flag2 +" | Flag3: "+ flag3 +" | Flag4: "+ flag4);
				
			if ((flag == 1) && (flag2 == 1) && (flag3 == 1) && (flag4 == 1))
			{	
				validateStatus = 1;
				msg = "SUCCESS";
			}
		}
		catch (Exception ex)
		{
			logger.trace(ex);
		}
			
		//Generating the Authentication Key Value
		authKeyVal = generateCourseAuthKey(registerNumber, courseId, validateStatus, 2);
			
		//System.out.println("validateStatus: "+ validateStatus +" | authKeyVal: "+ authKeyVal 
		// 		+" | emailOTP: "+ emailOTP +" | msg: "+ msg);
			
		return validateStatus +"|"+ authKeyVal +"|"+ emailOTP +"|"+ msg;
	}
	
	//Validate Course & OTP
	public String validateCourseAndOTP(String semesterSubId, String registerNumber, String courseId, 
						String emailOTP, String IpAddress, String processType)
	{	
		int validateStatus = 2, otpReasonType = 0, otpAllowFlag = 2, flag = 2, flag2 = 2, flag3 = 2;							
		String msg = "NONE", courseCode = "", emailOTPDB = "NONE";
		
		CourseCatalogModel ccm = new CourseCatalogModel();
		List<Object[]> objectList = new ArrayList<Object[]>();
		
		/*System.out.println("semesterSubId: "+ semesterSubId +" | registerNumber: "+ registerNumber 
				+" | courseId: "+ courseId +" | emailOTP: "+ emailOTP +" | IpAddress: "+ IpAddress 
				+" | processType: "+ processType);*/
		
		try
		{
			//Checking the select course is valid or not
			if ((courseId != null) && (!courseId.equals("")))
			{
				ccm = courseCatalogService.getOne(courseId);
				if (ccm != null)
				{
					courseCode = ccm.getCode();										
					flag = 1;
				}
				else
				{
					msg = "Invalid course...!";
				}
			}
			else
			{
				msg = "Invalid course...!";
			}
			
			//Validating the Process Type.
			if (flag == 1)
			{
				if (processType.equals("DELETE"))
				{
					otpReasonType = 2;
					flag2 = 1;
				}
				else if (processType.equals("MODIFY"))
				{
					otpReasonType = 3;
					flag2 = 1;
				}
				else
				{
					msg = "Invalid process type.  Kindly proceed once again."; 
				}
			}
						
			//Checking the OTP
			if (flag2 == 1)
			{
				objectList = courseRegistrationWithdrawService.getCourseWithdrawOTP(semesterSubId, registerNumber, 
									courseId, otpReasonType);
				if (!objectList.isEmpty())
				{
					for (Object[] e: objectList)
					{
						emailOTPDB = e[1].toString();
						break;
					}
					
					if (emailOTPDB.equals(emailOTP))
					{
						flag3 = 1;
					}
					else
					{
						otpAllowFlag = 1;
						msg = "You entered the wrong OTP, kindly enter your E-Mail OTP properly "+
								"for the course "+ courseCode +"."; 
					}
				}
				else
				{
					msg = "You did not receive OTP for the course "+ courseCode +".  Kindly proceed once again."; 
				}
			}
			//System.out.println("Flag: "+ flag +" | Flag2: "+ flag2 +" | Flag3: "+ flag3);
			
			if ((flag == 1) && (flag2 == 1) && (flag3 == 1))
			{	
				//Confirm the OTP Status
				courseRegistrationWithdrawService.modifyWithdrawOTPConfirmationStatus(semesterSubId, registerNumber, 
						courseId, otpReasonType, 2, 2, registerNumber, IpAddress);
				
				validateStatus = 1;
				msg = "SUCCESS";
			}
		}
		catch (Exception ex)
		{
			logger.trace(ex);
		}
					
		//System.out.println("validateStatus: "+ validateStatus +" | otpAllowFlag: "+ otpAllowFlag +" | msg: "+ msg);
			
		return validateStatus +"|"+ otpAllowFlag +"|"+ msg;
	}
	
	public String getRandomOtp(Integer keyLength)
	{
		String sDefaultChars = "123456789abcdefghjkmnpqrxyzABCDEFGHJKMNPQRXYZ";
		Integer iKeyLength =keyLength;
		Integer iDefaultCharactersLength = sDefaultChars.length();
		String sMyKey="";
		for(int iCounter=1;iCounter<=iKeyLength;iCounter++)
		{
			Integer iPickedChar=(int) ((iDefaultCharactersLength*Math.random())+1);
			if(iPickedChar>=sDefaultChars.length())
				sMyKey = sMyKey+(sDefaultChars.substring(sDefaultChars.length()-1));
			else
				sMyKey = sMyKey+(sDefaultChars.substring(iPickedChar,iPickedChar+1));
		}
		
		return sMyKey;
	}
	
	//Course Option Allow Status or Count.
	//Status Type=> 1: Fresh Course Status/ 2: N Grade Course Status/ 3: Extra Option Allow Status
	//				4: PE Additional Allow Status
	public int getCourseStatusOrCount(int statusType, String programGroupCode, String programSpecCode, 
					int studentGraduateYear, int acadGraduateYear, int semesterId, String semesterSubId, 
					int studentAdmissionYear)
	{
		int tempReturnValue = 0, regularCourseStatus = 2, NGradeCourseStatus = 2, eoStatus = 2, peAdlStatus = 2;
		
		if (semesterId == 7)
		{
			if (programGroupCode.equals("MTECH") || programGroupCode.equals("MCA"))
			{
				regularCourseStatus = 1;
				NGradeCourseStatus = 1;
				
				if (studentAdmissionYear == 2020)
				{
					peAdlStatus = 1;
				}
			}
			else if (programGroupCode.equals("RP"))
			{
				regularCourseStatus = 1;
				NGradeCourseStatus = 1;
			}
			else if (programGroupCode.equals("MTECH5") && (studentGraduateYear <= (acadGraduateYear+1)))
			{
				regularCourseStatus = 1;
				NGradeCourseStatus = 1;
			}
			else if (studentGraduateYear <= acadGraduateYear)
			{
				regularCourseStatus = 1;
				NGradeCourseStatus = 1;
			}
			else
			{
				NGradeCourseStatus = 1;
			}
		}
		else if ((semesterId == 8) || (semesterId == 9))
		{
			if (programGroupCode.equals("MCA"))
			{
				regularCourseStatus = 1;
				NGradeCourseStatus = 1;
			}
			else if (programGroupCode.equals("MTECH5") && (studentGraduateYear <= (acadGraduateYear+1)))
			{
				regularCourseStatus = 1;
				NGradeCourseStatus = 1;
			}
			else if (studentGraduateYear <= acadGraduateYear)
			{
				regularCourseStatus = 1;
				NGradeCourseStatus = 1;
			}
			else
			{
				NGradeCourseStatus = 1;
			}
		}
		else
		{
			regularCourseStatus = 1;
			NGradeCourseStatus = 1;
			eoStatus = 1;
			peAdlStatus = 1;
		}
		
		if (statusType == 1)
		{
			tempReturnValue = regularCourseStatus;
		}
		else if (statusType == 2)
		{
			tempReturnValue = NGradeCourseStatus;
		}
		else if (statusType == 3)
		{
			tempReturnValue = eoStatus;
		}
		else if (statusType == 4)
		{
			tempReturnValue = peAdlStatus;
		}
		
		return tempReturnValue;
	}
	
	
	public List<Object[]> getRegistrationOption(String programGroupCode, String courseSystem, int rgrCourseAllowStatus, 
								int reRegCourseAllowStatus, int peueAllowStatus, Integer specializationId, Integer admissionYear, 
								Float curriculumVersion)
	{
		List<Object[]> returnObjectList = new ArrayList<Object[]>();
		List<ProgrammeSpecializationCurriculumCategoryCredit> pscccList = new ArrayList<ProgrammeSpecializationCurriculumCategoryCredit>();
		
		if ((programGroupCode == null) || programGroupCode.equals(""))
		{
			programGroupCode = "NONE";
		}
		
		if ((courseSystem == null) || courseSystem.equals(""))
		{
			courseSystem = "NONE";
		}
		
		if (specializationId == null)
		{
			specializationId = 0;
		}
		
		if (admissionYear == null)
		{
			admissionYear = 0;
		}
		
		if (curriculumVersion == null)
		{
			curriculumVersion = 0F;
		}
		
		
		if ((!programGroupCode.equals("NONE")) && (programGroupCode.equals("RP") || programGroupCode.equals("IEP")))
		{
			returnObjectList.add(new Object[] {"RGR", "Regular"});
			if (reRegCourseAllowStatus == 1)
			{
				returnObjectList.add(new Object[] {"RR", "Re - Registration"});
			}
		}
		else if ((!programGroupCode.equals("NONE")) && (!courseSystem.equals("NONE")) 
						&& (courseSystem.equals("NONFFCS") || courseSystem.equals("FFCS")))
		{
			if (rgrCourseAllowStatus == 1)
			{
				returnObjectList.add(new Object[] {"RGR", "Regular"});
			}
			returnObjectList.add(new Object[] {"FFCSCAL", "FFCS to CAL Course Equivalence"});
			if (reRegCourseAllowStatus == 1)
			{
				returnObjectList.add(new Object[] {"RR", "Re - Registration"});
			}
		}
		else if ((!programGroupCode.equals("NONE")) && (!courseSystem.equals("NONE")) 
						&& (!courseSystem.equals("NONFFCS")) && (!courseSystem.equals("FFCS")))
		{
			if (rgrCourseAllowStatus == 1)
			{
				pscccList = programmeSpecializationCurriculumCreditService.getBySpecializationIdAdmissionYearAndVersion( 
								specializationId, admissionYear, curriculumVersion);
				if (!pscccList.isEmpty())
				{
					for (ProgrammeSpecializationCurriculumCategoryCredit e : pscccList)
					{
						if (peueAllowStatus == 1)
						{
							returnObjectList.add(new Object[] {e.getId().getCourseCategory(), e.getCurriculumCategoryMaster().getDescription()});
						}
						else if ((peueAllowStatus == 2) && (e.getId().getCourseCategory().equals("PC") || e.getId().getCourseCategory().equals("UC")))
						{
							returnObjectList.add(new Object[] {e.getId().getCourseCategory(), e.getCurriculumCategoryMaster().getDescription()});
						}
					}
				}
			}
			if (reRegCourseAllowStatus == 1)
			{
				returnObjectList.add(new Object[] {"RR", "Re - Registration"});
			}
		}
		//System.out.println("returnObjectList size: "+ returnObjectList.size());
		
		return returnObjectList;
	}
	
	//Get the Clash Status (New Method)
	public Map<Long, Object[]> getClashSlotStatus2(List<Object[]> registeredList, List<CourseAllocationModel> courseAllocationList, 
									Map<String, List<SlotTimeMasterModel>> slotTimeMapList)
	{
		Map<Long, Object[]> returnMapList = new HashMap<Long, Object[]>();
				
		int checkStatus = 2, patternId = 0;
		String clashSlot = "", checkMessage = "", color = "";
		String[] clashStatusArray = new String[]{};
		List<String> classIdList = new ArrayList<String>();
		List<Long> slotIdList = new ArrayList<Long>();
		//System.out.println("Checking clash status with registered Slots==>");
		
		if ((!registeredList.isEmpty()) && (!courseAllocationList.isEmpty()))
		{
			
			classIdList = registeredList.stream().map(e -> e[3].toString()).collect(Collectors.toList());
			slotIdList = registeredList.stream().map(e -> Long.parseLong(e[2].toString())).collect(Collectors.toList());
			//System.out.println("classIdLists: "+ classIdList +" | slotIdList: "+ slotIdList));
			
			//Checking the clash with Registered Slot
			for (CourseAllocationModel e : courseAllocationList)
			{
				patternId = e.getTimeTableModel().getPatternId();
				clashSlot = e.getTimeTableModel().getClashSlot();
				
				if (!returnMapList.containsKey(e.getSlotId()))
				{
					checkStatus = 2;
					checkMessage = "NONE";
					color = "green";
				
					if ((e.getSlotId() <= 0) || (e.getCourseType().equals("EPJ")))
					{
						checkStatus = 1;
					}
					else if (classIdList.contains(e.getClassId()))
					{
						checkMessage = "Registered Slot";
						color = "red";
					}
					else if ((!classIdList.contains(e.getClassId())) && (slotIdList.contains(e.getSlotId())))
					{
						checkMessage = "Similar to Registered Slot";
						color = "brown";
					}
					else
					{
						clashStatusArray = courseAllocationService.getClashStatus(patternId, clashSlot, registeredList, slotTimeMapList).split("\\|");
						checkStatus = Integer.parseInt(clashStatusArray[0].toString());
												
						if (checkStatus == 2)
						{
							checkMessage = "Clashed with "+ clashStatusArray[1].toString();
							color = "red";
						}
					}
					
					returnMapList.put(e.getSlotId(), new Object[] {checkMessage, color});
				}
			}			
		}
								
		return returnMapList;
	}	
	
	//Get the Slot Information
	public Map<String, Object[]> getSlotInfo(List<Object[]> registeredList, List<CourseAllocationModel> courseAllocationList, 
									Map<String, List<SlotTimeMasterModel>> slotTimeMapList)
	{
		Map<String, Object[]> returnMapList = new HashMap<String, Object[]>();
				
		int patternId = 0, checkStatus = 2;
		long buildingId = 0;
		String slot = "", clashSlot = "", buildingCode = "", checkMessage = "", color = "";
		
		String[] clashStatusArray = new String[]{}, clashStatusArray2 = new String[]{};
		List<String> classIdList = new ArrayList<String>();
		List<Long> slotIdList = new ArrayList<Long>();
		Map<String, Integer> sfiMapList = new HashMap<String, Integer>();
		//System.out.println("Checking Info with registered Slots==>");
		
		if ((!registeredList.isEmpty()) && (!courseAllocationList.isEmpty()))
		{	
			classIdList = registeredList.stream().map(e -> e[3].toString()).collect(Collectors.toList());
			slotIdList = registeredList.stream().map(e -> Long.parseLong(e[2].toString())).collect(Collectors.toList());
			sfiMapList = courseAllocationService.getSlotFixedInfoList();
			//System.out.println("classIdLists: "+ classIdList +" | slotIdList: "+ slotIdList 
			//		+" | sfiMapList size: "+ sfiMapList.size());
			
			for (CourseAllocationModel e : courseAllocationList)
			{
				patternId = e.getTimeTableModel().getPatternId();
				slot = e.getTimeTableModel().getSlotName();
				clashSlot = e.getTimeTableModel().getClashSlot();
				buildingId = e.getBuildingMasterBuildingId();
				buildingCode = e.getRoomMaster().getBuildingMaster().getCode();
				
				if (!returnMapList.containsKey(e.getClassId()))
				{
					checkStatus = 2;
					checkMessage = "NONE";
					color = "green";
					
					if (classIdList.contains(e.getClassId()))
					{
						checkMessage = "Registered Slot";
						color = "red";
					}
					else if ((!classIdList.contains(e.getClassId())) && (slotIdList.contains(e.getSlotId())))
					{
						checkMessage = "Similar to Registered Slot";
						color = "brown";
					}
					else if ((e.getSlotId() > 0) && (!e.getCourseType().equals("EPJ")))
					{
						clashStatusArray = courseAllocationService.getClashStatus(patternId, clashSlot, registeredList, slotTimeMapList).split("\\|");
						checkStatus = Integer.parseInt(clashStatusArray[0].toString());
												
						if (checkStatus == 1)
						{
							clashStatusArray2 = courseAllocationService.getSlotInfoStatus(patternId, slot, buildingId, buildingCode, 
													registeredList, slotTimeMapList, sfiMapList).split("\\|");
							checkMessage = clashStatusArray2[1].toString();
							color = clashStatusArray2[2].toString();
						}
						else
						{
							checkMessage = "Clashed with "+ clashStatusArray[1].toString();
							color = "red";
						}
					}
					
					returnMapList.put(e.getClassId(), new Object[] {checkMessage, color});
				}
			}			
		}
							
		return returnMapList;
	}
}