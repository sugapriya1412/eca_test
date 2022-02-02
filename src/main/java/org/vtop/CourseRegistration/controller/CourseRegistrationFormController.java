package org.vtop.CourseRegistration.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.vtop.CourseRegistration.model.CourseAllocationModel;
import org.vtop.CourseRegistration.model.CourseCatalogModel;
import org.vtop.CourseRegistration.model.ProjectRegistrationModel;
import org.vtop.CourseRegistration.model.ProjectRegistrationPKModel;
import org.vtop.CourseRegistration.model.SlotTimeMasterModel;
import org.vtop.CourseRegistration.service.CourseAllocationService;
import org.vtop.CourseRegistration.service.CourseCatalogService;
import org.vtop.CourseRegistration.service.CourseRegistrationCommonFunction;
import org.vtop.CourseRegistration.service.CourseRegistrationService;
import org.vtop.CourseRegistration.service.CourseRegistrationWaitingService;
import org.vtop.CourseRegistration.service.ProjectRegistrationService;
import org.vtop.CourseRegistration.service.RegistrationLogService;
import org.vtop.CourseRegistration.service.SemesterMasterService;
import org.vtop.CourseRegistration.service.StudentHistoryService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Controller
public class CourseRegistrationFormController 
{	
	@Autowired private CourseCatalogService courseCatalogService;
	@Autowired private RegistrationLogService registrationLogService;
	@Autowired private CourseAllocationService courseAllocationService;
	@Autowired private CourseRegistrationService courseRegistrationService;
	@Autowired private StudentHistoryService studentHistoryService;
	@Autowired private CourseRegistrationCommonFunction courseRegCommonFn;
	@Autowired private CourseRegistrationWaitingService courseRegistrationWaitingService;
	@Autowired private ProjectRegistrationService projectRegistrationService;
	@Autowired private SemesterMasterService semesterMasterService;
	
	private static final Logger logger = LogManager.getLogger(CourseRegistrationFormController.class);	
	private static final String[] classType = { "BFS" };
	private static final String RegErrorMethod = "WS2122REG";
	
	private static final String CAMPUSCODE = "VLR";	
	private static final int BUTTONS_TO_SHOW = 5;
	private static final int INITIAL_PAGE = 0;
	private static final int INITIAL_PAGE_SIZE = 5;
	private static final int[] PAGE_SIZES = { 5, 10, 15, 20 };
	
	
	@PostMapping("viewRegistrationOption")
	public String viewRegistrationOption(Model model, HttpSession session, HttpServletRequest request, 
						HttpServletResponse response) 
	{
		String IpAddress = (String) session.getAttribute("IpAddress");
		String msg = null, infoMsg = "", urlPage = "";
		Integer updateStatus = 1;		
		int allowStatus = 2, regularFlag = 2;
		List<String> courseRegWaitingList = new ArrayList<String>();
						
		String registerNumber = (String) session.getAttribute("RegisterNumber");
		Integer PEUEAllowStatus = (Integer) session.getAttribute("PEUEAllowStatus");
		regularFlag = (Integer) session.getAttribute("regularFlag");
				
		model.addAttribute("regularFlag", regularFlag);
		model.addAttribute("PEUEAllowStatus", PEUEAllowStatus);
				
		try
		{
			int pageAuthStatus = 2;
			String pageAuthKey = "";
			pageAuthKey = (String) session.getAttribute("pageAuthKey");
			pageAuthStatus = courseRegCommonFn.validatePageAuthKey(pageAuthKey, registerNumber, 2);
			
			if ((registerNumber != null) && (pageAuthStatus == 1))
			{
				session.setAttribute("pageAuthKey", courseRegCommonFn.generatePageAuthKey(registerNumber, 2));
				
				String semesterSubId = (String) session.getAttribute("SemesterSubId");
				String[] classGroupId = session.getAttribute("classGroupId").toString().split("/");
				Integer WaitingListStatus = (Integer) session.getAttribute("waitingListStatus");
												
				Date startDate = (Date) session.getAttribute("startDate");
				Date endDate = (Date) session.getAttribute("endDate");
				String startTime = (String) session.getAttribute("startTime");
				String endTime = (String) session.getAttribute("endTime");
							
				
				String returnVal = courseRegCommonFn.AddorDropDateTimeCheck(startDate, endDate, startTime, endTime, 
										registerNumber, updateStatus, IpAddress);
				String[] statusMsg = returnVal.split("/");
				allowStatus = Integer.parseInt(statusMsg[0]);
				infoMsg = statusMsg[1];
				String registrationOption = "";
				Integer pageSize = 5;
				Integer page = 1;
				Integer searchType = 0;
				String searchVal = "";
								
				switch(allowStatus)
				{
					case 1:
						if (WaitingListStatus == 1)
						{
							courseRegWaitingList = courseRegistrationWaitingService.getWaitingCourseByClassGroupId(
																semesterSubId, registerNumber, classGroupId);
						}
						registrationOption = "CECA";
						callCourseRegistrationTypes(registrationOption, pageSize, page, searchType, searchVal, session, model);
						
						model.addAttribute("courseRegWaitingList", courseRegWaitingList);
						model.addAttribute("registrationOption", registrationOption);					
						model.addAttribute("showFlag", 1);
						
						urlPage = "mainpages/CourseList :: section";
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
		catch(Exception ex)
		{
			model.addAttribute("flag", 1);
			registrationLogService.addErrorLog(ex.toString(), RegErrorMethod+"CourseRegistrationFormController", 
					"viewRegistrationOption", registerNumber, IpAddress);
			registrationLogService.UpdateLogoutTimeStamp2(IpAddress,registerNumber);
			urlPage = "redirectpage";
			return urlPage;			
		}
		model.addAttribute("info", msg);
		return urlPage;
	}
	
	
	@PostMapping("processFFCStoCal")
	public String processFFCStoCal(Model model, HttpServletRequest request, HttpSession session) 
	{	
		String registerNumber = (String) session.getAttribute("RegisterNumber");
		String IpAddress = (String) session.getAttribute("IpAddress");
		
		String msg = null, urlPage = "", infoMsg = "", subCourseOption ="",pageAuthKey = "";
		String registrationOption = "FFCSCAL";
		int allowStatus = 2,pageAuthStatus = 2;		
		Integer updateStatus = 1, page = 1;		
				
		try
		{	
			pageAuthKey = (String) session.getAttribute("pageAuthKey");
			pageAuthStatus = courseRegCommonFn.validatePageAuthKey(pageAuthKey, registerNumber, 2);
			
			if ((registerNumber != null) && (pageAuthStatus == 1))
			{	
				session.setAttribute("pageAuthKey", courseRegCommonFn.generatePageAuthKey(registerNumber, 2));
				
				Date startDate = (Date) session.getAttribute("startDate");
				Date endDate = (Date) session.getAttribute("endDate");
				String startTime = (String) session.getAttribute("startTime");
				String endTime = (String) session.getAttribute("endTime");
				
				String returnVal = courseRegCommonFn.AddorDropDateTimeCheck(startDate, endDate, startTime, endTime, 
										registerNumber, updateStatus, IpAddress);
				String[] statusMsg = returnVal.split("/");
				allowStatus = Integer.parseInt(statusMsg[0]);
				infoMsg = statusMsg[1];		
				
				switch (allowStatus)
				{
					case 1:
						urlPage = processRegistrationOption(registrationOption, model, session, 5, page, 0, "NONE", 
									subCourseOption, request);
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
			urlPage = "redirectpage";
			registrationLogService.addErrorLog(e.toString(), RegErrorMethod+"CourseRegistrationFormController", 
					"processFFCStoCal", registerNumber, IpAddress);
			registrationLogService.UpdateLogoutTimeStamp2(IpAddress,registerNumber);
			return urlPage;
		}		
		model.addAttribute("info", msg);
		return urlPage;
	}	
	

	@PostMapping("processRegistrationOption")
	public String processRegistrationOption(@RequestParam(value = "registrationOption", required = false) String registrationOption, 
						Model model, HttpSession session, @RequestParam(value = "pageSize", required = false) Integer pageSize,
						@RequestParam(value = "page", required = false) Integer page,
						@RequestParam(value = "searchType", required = false) Integer searchType,
						@RequestParam(value = "searchVal", required = false) String searchVal,
						@RequestParam(value = "subCourseOption", required = false) String subCourseOption, HttpServletRequest request)
	{
		String registerNumber = (String) session.getAttribute("RegisterNumber");
		String IpAddress = (String) session.getAttribute("IpAddress");
		
		String flagValue = request.getParameter("flag");
		if ((flagValue == null) || (flagValue.equals(null)))
		{
			flagValue = "0";
		}
		
		String msg = null, infoMsg = "", urlPage = "";				
		Integer updateStatus = 1;
		int allowStatus = 2;
				
		if ((registrationOption != null) && (!registrationOption.equals(null))) 
		{
			session.setAttribute("registrationOption", registrationOption);
		} 
		else 
		{
			registrationOption = (String) session.getAttribute("registrationOption");
		}
		
		int pageAuthStatus = 2;
		String pageAuthKey = "";
		pageAuthKey = (String) session.getAttribute("pageAuthKey");
		pageAuthStatus = courseRegCommonFn.validatePageAuthKey(pageAuthKey, registerNumber, 2);
		
		try
		{
			if ((registerNumber!=null) && (pageAuthStatus == 1))
			{
				session.setAttribute("pageAuthKey", courseRegCommonFn.generatePageAuthKey(registerNumber, 2));
				String semesterSubId = (String) session.getAttribute("SemesterSubId");
				int studyStartYear = (int) session.getAttribute("StudyStartYear");
				Integer StudentGraduateYear = (Integer) session.getAttribute("StudentGraduateYear");
				Integer semesterId  = (Integer) session.getAttribute("SemesterId");
				String ProgramGroupCode = (String) session.getAttribute("ProgramGroupCode");
				Integer programGroupId = (Integer) session.getAttribute("ProgramGroupId");
				String ProgramSpecCode = (String) session.getAttribute("ProgramSpecCode");
				Integer programSpecId = (Integer) session.getAttribute("ProgramSpecId");
				String[] classGroupId = session.getAttribute("classGroupId").toString().split("/");
				String pOldRegisterNumber = (String) session.getAttribute("OldRegNo");
				
				@SuppressWarnings("unchecked")
				List<String> compCourseList = (List<String>) session.getAttribute("compulsoryCourseList");
				List<String> courseRegWaitingList = new ArrayList<String>();
				String costCentreCode = (String) session.getAttribute("costCentreCode");
				Integer WaitingListStatus = (Integer) session.getAttribute("waitingListStatus");
				Integer compulsoryCourseStatus = (Integer) session.getAttribute("compulsoryCourseStatus");
				Date startDate = (Date) session.getAttribute("startDate");
				Date endDate = (Date) session.getAttribute("endDate");
				String startTime = (String) session.getAttribute("startTime");
				String endTime = (String) session.getAttribute("endTime");
				
				String returnVal = courseRegCommonFn.AddorDropDateTimeCheck(startDate, endDate, startTime, endTime, 
										registerNumber, updateStatus, IpAddress);
				String[] statusMsg = returnVal.split("/");
				allowStatus = Integer.parseInt(statusMsg[0]);
				infoMsg = statusMsg[1];	
				
				int compulsoryStatus = 2;
				
				switch(allowStatus)
				{
					case 1:
						if (compulsoryCourseStatus == 1)
						{
							compulsoryStatus = courseRegCommonFn.compulsoryCourseCheck(programGroupId, studyStartYear, 
													StudentGraduateYear, semesterId, semesterSubId, registerNumber, 
													classGroupId, classType, ProgramSpecCode, programSpecId, 
													ProgramGroupCode, pOldRegisterNumber, compCourseList, costCentreCode);
							session.setAttribute("compulsoryCourseStatus", compulsoryStatus);
						}
						
						if (compulsoryStatus == 1)
						{	
							getCompulsoryCourseList(registrationOption, pageSize, page, searchType, searchVal, 
									subCourseOption, session, model, compCourseList);
							model.addAttribute("WaitingListStatus", WaitingListStatus);
							urlPage = "mainpages/CompulsoryCourseList :: section";
						}
						else
						{
							callCourseRegistrationTypes(registrationOption, pageSize, page, searchType, searchVal, session, model);
							if(WaitingListStatus==1)
							{
								courseRegWaitingList = courseRegistrationWaitingService.getWaitingCourseByClassGroupId(
																	semesterSubId, registerNumber, classGroupId);
							}
							model.addAttribute("courseRegWaitingList", courseRegWaitingList);
							model.addAttribute("WaitingListStatus", WaitingListStatus);
							model.addAttribute("studySystem", session.getAttribute("StudySystem"));
							model.addAttribute("registrationOption", registrationOption);					
							model.addAttribute("showFlag", 1);
							
							switch (flagValue)
							{
								case "1":
									urlPage = "mainpages/CourseList :: cclistfrag";
									break;
								default:
									urlPage = "mainpages/CourseList :: section";
									break;
							}
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
		catch (Exception ex) 
		{
			model.addAttribute("flag", 1);
			registrationLogService.addErrorLog(ex.toString(), RegErrorMethod+"CourseRegistrationFormController", 
					"processRegistrationOption", registerNumber, IpAddress);
			registrationLogService.UpdateLogoutTimeStamp2(IpAddress,registerNumber);
			urlPage = "redirectpage";
			return urlPage;
		}		
		return urlPage;
	}
	
	public int callCourseRegistrationTypes(String registrationOption, Integer pageSize, Integer page, 
					Integer searchType, String searchVal, HttpSession session, Model model)
	{
		String semesterSubId = (String) session.getAttribute("SemesterSubId");		
		String registerNo = (String) session.getAttribute("RegisterNumber");
		
		try
		{
			if (semesterSubId != null)
			{
				Integer programGroupId = (Integer) session.getAttribute("ProgramGroupId");
				String ProgramGroupCode = (String) session.getAttribute("ProgramGroupCode");
				Integer ProgramSpecId = (Integer) session.getAttribute("ProgramSpecId");
				String ProgramSpecCode = (String) session.getAttribute("ProgramSpecCode");
				Integer studYear = (Integer) session.getAttribute("StudyStartYear");
				Float curriculumVersion = (Float) session.getAttribute("curriculumVersion");
				
				@SuppressWarnings("unchecked")
				List<Integer> egbGroupId = (List<Integer>) session.getAttribute("EligibleProgramLs");
				String[] courseSystem = (String[]) session.getAttribute("StudySystem");				
				String[] registerNumber = (String[]) session.getAttribute("registerNumberArray");				
				String registrationMethod = (String) session.getAttribute("registrationMethod");
				Integer StudentGraduateYear = (Integer) session.getAttribute("StudentGraduateYear");
				Integer PEUEAllowStatus = (Integer) session.getAttribute("PEUEAllowStatus");
				String[] classGroupId = session.getAttribute("classGroupId").toString().split("/");
				String costCentreCode = (String) session.getAttribute("costCentreCode");
											
				Pager pager = null;		
				int evalPageSize = INITIAL_PAGE_SIZE;
				int evalPage = INITIAL_PAGE;
				evalPageSize = pageSize == null ? INITIAL_PAGE_SIZE : pageSize;
				evalPage = (page == null || page < 1) ? INITIAL_PAGE : page - 1;
				int pageSerialNo = evalPageSize * evalPage;
				int srhType = (searchType == null) ? 0 : searchType;
				String srhVal = (searchVal == null) ? "NONE" : searchVal;
				//String srhLike = null;				
				
				//System.out.println("pageSize: "+ pageSize +" | page: "+ page +" | pageSerialNo: "+ pageSerialNo 
				//		+" | evalPageSize: "+ evalPageSize +" | evalPage: "+ evalPage);
				
				if (registrationOption != null) 
				{
					session.setAttribute("registrationOption", registrationOption);
				} 
				else 
				{
					registrationOption = (String) session.getAttribute("registrationOption");
				}
				
				int totalPage = 0, pageNumber = evalPage; 
				String[] pagerArray = new String[]{};
				List<CourseCatalogModel> courseCatalogModelPageList = new ArrayList<CourseCatalogModel>();
							
				courseCatalogModelPageList = courseCatalogService.getCourseListForRegistration(registrationOption, 
												CAMPUSCODE, courseSystem, egbGroupId, programGroupId, semesterSubId, 
												ProgramSpecId, classGroupId, classType, studYear, curriculumVersion, 
												registerNo, srhType, srhVal, StudentGraduateYear, ProgramGroupCode, 
												ProgramSpecCode, registrationMethod, registerNumber, PEUEAllowStatus, 
												evalPage, evalPageSize, costCentreCode);
								
				//System.out.println("CourseListSize: "+ courseCatalogModelPageList.size() 
				//			+" | evalPageSize: "+ evalPageSize +" | pageNumber: "+ pageNumber);
				pagerArray = courseCatalogService.getTotalPageAndIndex(courseCatalogModelPageList.size(), 
								evalPageSize, pageNumber).split("\\|");
				totalPage = Integer.parseInt(pagerArray[0]);
				pager = new Pager(totalPage, pageNumber, BUTTONS_TO_SHOW);
				//System.out.println("totalPage: "+ totalPage);
							
				model.addAttribute("tlTotalPage", totalPage);
				model.addAttribute("tlPageNumber", pageNumber);
				model.addAttribute("tlCourseCatalogModelList", courseCatalogModelPageList);
				model.addAttribute("courseRegModelList", courseRegistrationService.getRegisteredCourseByClassGroup(semesterSubId, 
						registerNo, classGroupId));
				model.addAttribute("registrationOption", registrationOption);
				model.addAttribute("pageSlno", pageSerialNo);
				model.addAttribute("selectedPageSize", evalPageSize);
				model.addAttribute("pageSizes", PAGE_SIZES);
				model.addAttribute("srhType", srhType);
				model.addAttribute("srhVal", srhVal);
				model.addAttribute("pager", pager);
				model.addAttribute("page", page);
			}			
		}
		catch(Exception e)
		{
			session.invalidate();
		}
				
		return 1;
	}
	

	@PostMapping(value="processCourseRegistration")
	public String processCourseRegistration(String courseId, 
						@RequestParam(value = "page", required = false) Integer page,
						@RequestParam(value = "searchType", required = false) Integer searchType,
						@RequestParam(value = "searchVal", required = false) String searchVal, 
						Model model, HttpSession session, HttpServletRequest request)//M1 
	{			
		String IpAddress = (String) session.getAttribute("IpAddress");
		String semesterSubId = (String) session.getAttribute("SemesterSubId");
		String registerNumber = (String) session.getAttribute("RegisterNumber");
		Integer WaitingListStatus=(Integer) session.getAttribute("waitingListStatus");
		
		String urlPage = "", courseTypeDisplay = "", msg = null, message = null, courseOption = "",	
					genericCourseType = "", infoMsg = "";
		String courseCategory = "NONE", subCourseType = "", subCourseDate = "", courseCode = "", 
					genericCourseTypeDisplay = "", authKeyVal = "", corAuthStatus = "", ccCourseId = "";
		String[] regStatusArr = new String[50];
		Integer updateStatus = 1;
		int allowStatus = 2, regStatusFlag = 2, projectStatus = 2, regAllowFlag = 1, wlAllowFlag = 1, 
				audAllowFlag = 1, rgrAllowFlag=2, minAllowFlag = 2, honAllowFlag = 2, adlAllowFlag = 2, 
				RPEUEAllowFlag=2,csAllowFlag=2,RUCUEAllowFlag=2;
		int ethExistFlag = 2, epjExistFlag = 2, epjSlotFlag = 2,regularFlag=2;
				
		int pageAuthStatus = 2;
		String pageAuthKey = "";
		pageAuthKey = (String) session.getAttribute("pageAuthKey");
		pageAuthStatus = courseRegCommonFn.validatePageAuthKey(pageAuthKey, registerNumber, 2);
		
		try
		{			
			if ((semesterSubId != null) && (pageAuthStatus == 1))
			{						
				registerNumber = (String) session.getAttribute("RegisterNumber");					
				String[] pCourseSystem = (String[]) session.getAttribute("StudySystem");
				Integer pProgramGroupId = (Integer) session.getAttribute("ProgramGroupId"); 
				String pProgramGroupCode = (String) session.getAttribute("ProgramGroupCode");
				Integer pProgramSpecId = (Integer) session.getAttribute("ProgramSpecId");
				String pProgramSpecCode = (String) session.getAttribute("ProgramSpecCode");
				String pSemesterSubId = (String) session.getAttribute("SemesterSubId"); 
				Integer pSemesterId = (Integer) session.getAttribute("SemesterId");
				Float CurriculumVersion = (Float) session.getAttribute("curriculumVersion");
				String pOldRegisterNumber = (String) session.getAttribute("OldRegNo"); 
				Integer maxCredit = (Integer) session.getAttribute("maxCredit");
				Integer cclTotalCredit = (Integer) session.getAttribute("cclTotalCredit");
				
				String registrationOption = (String) session.getAttribute("registrationOption");
				String subCourseOption = (String) session.getAttribute("subCourseOption");
				Integer StudyStartYear = (Integer) session.getAttribute("StudyStartYear");
				Float curriculumVersion = (Float) session.getAttribute("curriculumVersion");
				Integer StudentGraduateYear = (Integer) session.getAttribute("StudentGraduateYear");
				Integer OptionNAStatus=(Integer) session.getAttribute("OptionNAStatus");
				
				Integer PEUEAllowStatus = (Integer) session.getAttribute("PEUEAllowStatus");
				String studentStudySystem = (String) session.getAttribute("studentStudySystem");
				String[] classGroupId = session.getAttribute("classGroupId").toString().split("/");
				String programGroupMode = (String) session.getAttribute("programGroupMode");
				String studentCgpaData = (String) session.getAttribute("studentCgpaData");
				String costCentreCode = (String) session.getAttribute("costCentreCode");
				Integer acadGraduateYear = (Integer) session.getAttribute("acadGraduateYear");
				
				regularFlag = (Integer) session.getAttribute("regularFlag");
				session.setAttribute("corAuthStatus", "NONE");
				session.setAttribute("authStatus", "NONE");
				session.setAttribute("camList", null);
				session.setAttribute("camList2", null);
				session.setAttribute("camList3", null);

				Date startDate = (Date) session.getAttribute("startDate");
				Date endDate = (Date) session.getAttribute("endDate");
				String startTime = (String) session.getAttribute("startTime");
				String endTime = (String) session.getAttribute("endTime");
					
				String returnVal = courseRegCommonFn.AddorDropDateTimeCheck(startDate, endDate, startTime, endTime, 
										registerNumber, updateStatus, IpAddress);
				String[] statusMsg = returnVal.split("/");
				allowStatus = Integer.parseInt(statusMsg[0]);
				infoMsg = statusMsg[1];
				
				List<String> courseTypeArr = new ArrayList<String>();					
				@SuppressWarnings("unchecked")
				List<String> registerNumberList = (List<String>) session.getAttribute("registerNumberList");
				@SuppressWarnings("unchecked")
				List<String> compCourseList = (List<String>) session.getAttribute("compulsoryCourseList");
				
				CourseCatalogModel courseCatalog = new CourseCatalogModel();
				List<CourseAllocationModel> list1 = new ArrayList<CourseAllocationModel>();
				List<CourseAllocationModel> ela = new ArrayList<CourseAllocationModel>();
				List<CourseAllocationModel> epj = new ArrayList<CourseAllocationModel>();
				List<CourseAllocationModel> courseAllocationList = new ArrayList<CourseAllocationModel>();
				
				courseCatalog = courseCatalogService.getOne(courseId);
				if (courseCatalog != null)
				{
					courseCode = courseCatalog.getCode();
					genericCourseType = courseCatalog.getGenericCourseType();
					genericCourseTypeDisplay = courseCatalog.getCourseTypeComponentModel().getDescription();
				}
					
				/*System.out.println(pCourseSystem +" | "+ pProgramGroupId +" | "+ pProgramGroupCode +" | "+
				pProgramSpecCode +" | "+ pSemesterSubId +" | "+ registerNumber +" | "+ 
				pOldRegisterNumber +" | "+ maxCredit +" | "+ courseId +" | "+ StudyStartYear+" | "+
				StudentGraduateYear +" | "+ studentStudySystem);*/
				
				switch(allowStatus)
				{
					case 1:
						regStatusArr = courseRegCommonFn.CheckRegistrationCondition(pCourseSystem, pProgramGroupId, 
											pProgramGroupCode, pProgramSpecCode, pSemesterSubId, registerNumber, 
											pOldRegisterNumber, maxCredit, courseId, StudyStartYear, StudentGraduateYear, 
											studentStudySystem, pProgramSpecId, CurriculumVersion, PEUEAllowStatus, 
											programGroupMode, classGroupId, studentCgpaData, WaitingListStatus, 
											OptionNAStatus, compCourseList, pSemesterId, classType, costCentreCode, 
											acadGraduateYear, cclTotalCredit).split("/");
						regStatusFlag = Integer.parseInt(regStatusArr[0]);
						message = regStatusArr[1];							
						courseOption = regStatusArr[2];
						regAllowFlag = Integer.parseInt(regStatusArr[3]);
						wlAllowFlag = Integer.parseInt(regStatusArr[4]);
						audAllowFlag = Integer.parseInt(regStatusArr[8]);
						rgrAllowFlag= Integer.parseInt(regStatusArr[11]);
						minAllowFlag = Integer.parseInt(regStatusArr[13]);
						honAllowFlag = Integer.parseInt(regStatusArr[12]);
						courseCategory = regStatusArr[14];
						adlAllowFlag = Integer.parseInt(regStatusArr[15]);
						authKeyVal = regStatusArr[16];
						RPEUEAllowFlag = Integer.parseInt(regStatusArr[17]);
						csAllowFlag = Integer.parseInt(regStatusArr[18]);
						RUCUEAllowFlag = Integer.parseInt(regStatusArr[19]);
						ccCourseId = regStatusArr[20];
						
						corAuthStatus = regStatusArr[2] +"/"+ regStatusArr[3] +"/"+ regStatusArr[4] 
											+"/"+ regStatusArr[8] +"/"+ regStatusArr[11] +"/"+ regStatusArr[13] 
											+"/"+ regStatusArr[12] +"/"+ regStatusArr[14] +"/"+ regStatusArr[15] 
											+"/"+ regStatusArr[17] +"/"+ regStatusArr[6] +"/"+ regStatusArr[7] 
											+"/"+ regStatusArr[9] +"/"+ regStatusArr[10] +"/"+ regStatusArr[18] 
											+"/"+ regStatusArr[19] +"/"+ regStatusArr[20];
			
						session.setAttribute("authStatus", authKeyVal);
						session.setAttribute("corAuthStatus", corAuthStatus);
												
						//System.out.println("corAuthStatus: "+ corAuthStatus);
						//System.out.println("AuthKeyVal: "+ authKeyVal);
						
						/*if(PEUEStatusFlag == 1)
						{*/
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
										courseTypeArr = semesterMasterService.getCourseTypeComponentByGenericType(genericCourseType);
									}
									break;
								
								default:
									courseTypeArr = semesterMasterService.getCourseTypeComponentByGenericType(genericCourseType);
									break;
							}
							
							if (genericCourseType.equals("ECA") && courseOption.equals("CS"))
							{
								subCourseOption = regStatusArr[7];
								subCourseType = regStatusArr[9];
								subCourseDate = regStatusArr[10];
							}
							else
							{
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
							}
								
							for (String crstp : courseTypeArr) 
							{
								if (courseTypeDisplay.equals(""))
								{
									courseTypeDisplay = semesterMasterService.getCourseTypeMasterByCourseType(crstp).getDescription();
								}
								else
								{
									courseTypeDisplay = courseTypeDisplay +" / "+ semesterMasterService.getCourseTypeMasterByCourseType(crstp).getDescription();
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
								
							if ((courseTypeArr.size() == 2) && (genericCourseType.equals("ETLP")) 
									&& (ethExistFlag == 1) && (epjExistFlag == 1))
							{
								epjSlotFlag = 1;
							}
							else if ((courseTypeArr.size() == 1) && (epjExistFlag == 1))
							{
								epjSlotFlag = 1;
							}
							//System.out.println("regStatusFlag: "+ regStatusFlag);
															
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
													epj = courseAllocationService.getCourseAllocationCourseIdTypeList(semesterSubId, 
																classGroupId, classType, courseId, "EPJ", pProgramGroupCode, 
																pProgramSpecCode, costCentreCode);
													model.addAttribute("cam3", epj);
													session.setAttribute("camList3", epj);
													break;
													
												case "ELA":
													ela = courseAllocationService.getCourseAllocationCourseIdTypeList(semesterSubId, 
															classGroupId, classType, courseId, "ELA", pProgramGroupCode, 
															pProgramSpecCode, costCentreCode);
													model.addAttribute("cam2", ela);
													session.setAttribute("camList2", ela);
													break;
													
												default:
													list1 = courseAllocationService.getCourseAllocationCourseIdTypeList(semesterSubId, 
																classGroupId, classType, courseId, crtp, pProgramGroupCode, 
																pProgramSpecCode, costCentreCode);
													model.addAttribute("cam", list1);
													session.setAttribute("camList", list1);
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
									
									//Assigning the all course type of allocation list to one course allocation list 
									if (!list1.isEmpty())
									{
										courseAllocationList.addAll(list1);
									}
									
									if (!ela.isEmpty())
									{
										courseAllocationList.addAll(ela);
									}
									//System.out.println("projectStatus: "+ projectStatus);
									
									if (projectStatus == 1)
									{
										List<Object[]> courseCostCentre = semesterMasterService.getEmployeeProfileByCampusCode(CAMPUSCODE);
										
										model.addAttribute("courseCostCentre", courseCostCentre);
										model.addAttribute("ProgramCode", session.getAttribute("ProgramGroupCode"));
										model.addAttribute("courseOption", courseOption);										
										
										urlPage = "mainpages/ProjectRegistration :: section";
									}
									else
									{											
										urlPage = "mainpages/CourseRegistration :: section";
									}
										
									model.addAttribute("shcssList", studentHistoryService.getStudentHistoryCS2(registerNumberList, 
												courseCode, studentStudySystem, pProgramSpecId, StudyStartYear, curriculumVersion, 
												semesterSubId, courseCategory, courseOption, ccCourseId));
									model.addAttribute("minorList", semesterMasterService.getAdditionalLearningTitleByLearnTypeGroupIdSpecIdAndCourseCode(
												minAllowFlag, "MIN", pProgramGroupId, pProgramSpecId, courseCode, studentStudySystem));
									model.addAttribute("honorList", semesterMasterService.getAdditionalLearningTitleByLearnTypeGroupIdSpecIdAndCourseCode(
												honAllowFlag, "HON", pProgramGroupId, pProgramSpecId, courseCode, studentStudySystem));
									model.addAttribute("courseOptionList",semesterMasterService.getRegistrationCourseOption(
												courseOption, genericCourseType, rgrAllowFlag, audAllowFlag, honAllowFlag, 
												minAllowFlag, adlAllowFlag, csAllowFlag, RPEUEAllowFlag, RUCUEAllowFlag));
									
									//model.addAttribute("tlClashMapList", courseRegCommonFn.getClashSlotStatus(semesterSubId, 
									//			registerNumber, courseAllocationList));
									callSlotInformation(model, semesterSubId, registerNumber, courseAllocationList);
									
									model.addAttribute("regAllowFlag", regAllowFlag);
									model.addAttribute("wlAllowFlag", wlAllowFlag);
									model.addAttribute("epjSlotFlag", epjSlotFlag);
									model.addAttribute("rgrAllowFlag", rgrAllowFlag);
									model.addAttribute("minAllowFlag", minAllowFlag);
									model.addAttribute("honAllowFlag", honAllowFlag);
									model.addAttribute("RPEUEAllowFlag", RPEUEAllowFlag);
									model.addAttribute("csAllowFlag", csAllowFlag);
									model.addAttribute("RUCUEAllowFlag", RUCUEAllowFlag);
									model.addAttribute("WaitingListStatus", WaitingListStatus);
									model.addAttribute("courseCatalogModel", courseCatalog);
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
									model.addAttribute("ProgramGroupCode", pProgramGroupCode);
									model.addAttribute("subCourseType", subCourseType);
									model.addAttribute("subCourseDate", subCourseDate);
									model.addAttribute("regularFlag", regularFlag);	
									model.addAttribute("tlCourseCategory", courseCategory);
									model.addAttribute("tlCompCourseList", compCourseList);
																		
									break;  
										
								case 2:
									model.addAttribute("infoMessage", message);									
									urlPage = processRegistrationOption(registrationOption, model, session, 5, page, searchType, 
													searchVal, subCourseOption, request);									
									break;  
							}					
							break;						
						/*}
						else
						{
							session.removeAttribute("registrationOption");
							model.addAttribute("studySystem", studentStudySystem);
							model.addAttribute("maxCredit", maxCredit);
							model.addAttribute("registrationMethod", (String)session.getAttribute("registrationMethod"));
							model.addAttribute("ProgramGroupCode", (String) session.getAttribute("ProgramGroupCode"));							
							model.addAttribute("showFlag", 0);
							urlPage = "mainpages/RegistrationOptionList::section";
						}*/
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
		catch(Exception ex)
		{
			//System.out.println(ex);
			model.addAttribute("flag", 1);
			registrationLogService.addErrorLog(ex.toString(), RegErrorMethod+"CourseRegistrationFormController", 
					"processCourseRegistration", registerNumber, IpAddress);
			registrationLogService.UpdateLogoutTimeStamp2(IpAddress,registerNumber);
			urlPage = "redirectpage";
			return urlPage;
		}		
		
		return urlPage;
	}
	
	
	@PostMapping(value="processRegisterProjectCourse")	
	public String processRegisterProjectCourse(	String costCentreId, String guideErpId, String projectTitle, 
						String courseOption, String courseCode, String courseType, String courseId, 
						String clashSlot, String classId, String projectDuration,String projectOption, 
						@RequestParam(value = "pageSize", required = false) Integer pageSize,
						@RequestParam(value = "page", required = false) Integer page,
						@RequestParam(value = "searchType", required = false) Integer searchType,
						@RequestParam(value = "searchVal", required = false) String searchVal,
						@RequestParam(value = "subCourseOption", required = false)  String subCourseOption, 
						Model model, HttpSession session, HttpServletRequest request) 
	{
		String IpAddress = (String) session.getAttribute("IpAddress");			
		String registerNumber = (String) session.getAttribute("RegisterNumber");
		String registrationOption = (String) session.getAttribute("registrationOption");
		List<CourseAllocationModel> projAllocationList=new ArrayList<CourseAllocationModel>();
		String msg = null, message = null, urlPage = "", pRegStatus = "", infoMsg = "", 
					csPjtMsg = "",RsemesterSubId="";
		String genericCourseType = "NONE", evaluationType = "NONE";
		Integer updateStatus = 1, projectStatus = 2, regStatus = 0;
		List<String> courseRegWaitingList = new ArrayList<String>();
		
		//Integer PEUEStatusFlag = (Integer) session.getAttribute("PEUEStatusFlag");
		String authStatus = (String) session.getAttribute("authStatus");
		String semesterSubId = (String) session.getAttribute("SemesterSubId");
		String studentCategory = (String) session.getAttribute("studentCategory");
		Integer approvalStatus = (Integer) session.getAttribute("approvalStatus");
				
		int authCheckStatus = courseRegCommonFn.validateCourseAuthKey(authStatus, registerNumber, courseId, 1);
		int csPjtFlag = 2, allowStatus = 2;
		int checkGraduateYear = 2020 ,maxCredit = 27, minCredit = 16;
		CourseCatalogModel ccm = new CourseCatalogModel();
		int pageAuthStatus = 2;
		String pageAuthKey = "";
		pageAuthKey = (String) session.getAttribute("pageAuthKey");
		pageAuthStatus = courseRegCommonFn.validatePageAuthKey(pageAuthKey, registerNumber, 2);
		
		try
		{
			if ((registerNumber != null) && (pageAuthStatus == 1) && (authCheckStatus == 1))
			{
				int studyStartYear = (int) session.getAttribute("StudyStartYear");
				Integer StudentGraduateYear = (Integer) session.getAttribute("StudentGraduateYear");
				Integer semesterId  = (Integer) session.getAttribute("SemesterId");
				Integer programGroupId = (Integer) session.getAttribute("ProgramGroupId");
				String ProgramGroupCode = (String) session.getAttribute("ProgramGroupCode");
				Integer programSpecId = (Integer) session.getAttribute("ProgramSpecId");
				String ProgramSpecCode = (String) session.getAttribute("ProgramSpecCode");
				String[] classGroupId = session.getAttribute("classGroupId").toString().split("/");
				String pOldRegisterNumber = (String) session.getAttribute("OldRegNo");
				
				@SuppressWarnings("unchecked")
				List<String> compCourseList = (List<String>) session.getAttribute("compulsoryCourseList");
				String costCentreCode = (String) session.getAttribute("costCentreCode");
				Integer WaitingListStatus=(Integer) session.getAttribute("waitingListStatus");
				Integer compulsoryCourseStatus = (Integer) session.getAttribute("compulsoryCourseStatus");
				Integer PEUEAllowStatus = (Integer) session.getAttribute("PEUEAllowStatus");
				Integer regularFlag = (Integer) session.getAttribute("regularFlag");
				Integer reRegFlag =  (Integer) session.getAttribute("reRegFlag");
				String registrationMethod = (String) session.getAttribute("registrationMethod");
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
				
				int compulsoryStatus = 2;
				
				ccm = courseCatalogService.getOne(courseId);
				if (ccm != null)
				{
					genericCourseType = ccm.getGenericCourseType();
					evaluationType = ccm.getEvaluationType();
				}
					
				switch(allowStatus)
				{
					case 1:
						if(projectOption.equals("PAT"))
						{
							projAllocationList = courseAllocationService.getCourseAllocationCourseIdTypeEmpidList(
													semesterSubId, classGroupId, classType, courseId, courseType, 
													"PAT", ProgramGroupCode, ProgramSpecCode, costCentreCode);
							if (projAllocationList.isEmpty())
							{
								csPjtFlag = 2;
								csPjtMsg="PAT Project Not Available/Allocated.";
							}
							else
							{
								csPjtFlag = 1;
							}
							
							if (csPjtFlag == 1)
							{
								for (CourseAllocationModel pjtCam : projAllocationList)
								{
									classId = pjtCam.getClassId();
								}
							}
							
							projectTitle = "NONE";
							guideErpId = "PAT";
						}
						else
						{
							csPjtFlag = 1;
						}
						//System.out.println(csPjtFlag +" | "+ courseOption);
							
						if (csPjtFlag == 1)
						{
							//Get Registration Status
							regStatus = courseRegCommonFn.getRegistrationStatus(approvalStatus, courseOption, 
											genericCourseType, evaluationType, studentCategory);
							
							//Project_Duration Assign
							switch(evaluationType)
							{
								case "CAPSTONE":
								case "GUIDE":
									projectStatus = 1;
									RsemesterSubId = semesterSubId;
									
									if((projectDuration != null) && (!projectDuration.equals("")) 
											&& projectDuration.equals("12"))
									{
										RsemesterSubId = "VL20202105";
									}
									
									break;
							}
							
							/*System.out.println(semesterSubId +", "+ classId +", "+ registerNumber +", "+ courseId +", "+ courseType +", "+ courseOption +", "+ regStatus 
										+", 0, "+ registerNumber +", "+ IpAddress +", GEN, "+ subCourseOption +", INSERT");*/
							
							pRegStatus = courseRegistrationService.courseRegistrationAdd2(semesterSubId, classId, registerNumber, 
												courseId, courseType, courseOption, regStatus, 0, registerNumber, IpAddress, 
												"GEN", subCourseOption, "INSERT", "", "");
							if (pRegStatus.equals("SUCCESS"))
							{
								msg = "Selected Project Course Successfully Registered";
							}
							else if ((pRegStatus.equals("FAIL")) || (pRegStatus.substring(0, 5).equals("error")))
							{
									message = "Technical error.";
									registrationLogService.addErrorLog(pRegStatus.toString()+"<-CODE->"+courseId, RegErrorMethod+"CourseRegistrationFormController", 
											"processRegisterProjectCourseINSERT PROC", registerNumber, IpAddress);
									registrationLogService.UpdateLogoutTimeStamp2(IpAddress,registerNumber);
							}
							else
							{
									message = pRegStatus;
							}						
								
							if ((projectStatus == 1) && (pRegStatus.equals("SUCCESS")))
							{	
								ProjectRegistrationPKModel projectRegistrationPKModel = new ProjectRegistrationPKModel();
								ProjectRegistrationModel projectRegistrationModel= new ProjectRegistrationModel();
								
								projectRegistrationPKModel.setClassId(classId);
								projectRegistrationPKModel.setRegisterNumber(registerNumber);
								projectRegistrationModel.setProjectRegistrationPKId(projectRegistrationPKModel);								
								projectRegistrationModel.setSemesterSubId(semesterSubId);
								projectRegistrationModel.setCourseId(courseId);
								projectRegistrationModel.setSlotId(0);
								projectRegistrationModel.setCourseType(courseType);
								projectRegistrationModel.setProjectTitle(projectTitle);			
								projectRegistrationModel.setGuideErpid(guideErpId);
								projectRegistrationModel.setProjectDuration(Integer.parseInt(projectDuration));
								projectRegistrationModel.setResultSemesterSubId(RsemesterSubId);
								projectRegistrationModel.setInternalFoilcardNumber(0L);
								projectRegistrationModel.setExternalFoilcardNumber(0L);
								projectRegistrationModel.setProjectOption(projectOption);
								projectRegistrationService.saveOne(projectRegistrationModel);
								
								//Fixing the Minimum & Maximum credit
								String[] creditLimitArr = courseRegCommonFn.getMinimumAndMaximumCreditLimit(semesterSubId, 
																registerNumber, ProgramGroupCode, costCentreCode, 
																studyStartYear, StudentGraduateYear, checkGraduateYear, 
																semesterId, ProgramSpecCode).split("\\|");
								minCredit = Integer.parseInt(creditLimitArr[0]);
								maxCredit = Integer.parseInt(creditLimitArr[1]);
								session.setAttribute("minCredit", minCredit);
								session.setAttribute("maxCredit", maxCredit);
							}							
						}
						else
						{
							message = csPjtMsg;
						}
												
						
						if (compulsoryCourseStatus == 1)
						{
							compulsoryStatus = courseRegCommonFn.compulsoryCourseCheck(programGroupId, studyStartYear, 
													StudentGraduateYear, semesterId, semesterSubId, registerNumber, 
													classGroupId, classType, ProgramSpecCode, programSpecId, 
													ProgramGroupCode, pOldRegisterNumber, compCourseList, costCentreCode);
							session.setAttribute("compulsoryCourseStatus", compulsoryStatus);
						}
						
						if (compulsoryStatus == 1)
						{
							getCompulsoryCourseList(registrationOption, pageSize, page, searchType, searchVal, 
									subCourseOption, session, model, compCourseList);							
							urlPage = "mainpages/CompulsoryCourseList :: section";
						}
						else
						{
							if (registrationOption.equals("COMP"))
							{
								session.removeAttribute("registrationOption");
								
								//model.addAttribute("regularFlag", session.getAttribute("regularFlag"));
								//model.addAttribute("PEUEAllowStatus", session.getAttribute("PEUEAllowStatus"));
								//model.addAttribute("registrationMethod", session.getAttribute("registrationMethod"));
								
								model.addAttribute("regOptionList", courseRegCommonFn.getRegistrationOption(ProgramGroupCode, 
										registrationMethod, regularFlag, reRegFlag, PEUEAllowStatus, programSpecId, studyStartYear, 
										curriculumVersion));
								
								model.addAttribute("studySystem", session.getAttribute("StudySystem"));
								model.addAttribute("showFlag", 0);
								model.addAttribute("info", msg);
								
								urlPage = "mainpages/RegistrationOptionList :: section";
							}
							else
							{	
								model.addAttribute("info", msg);								
								if(WaitingListStatus == 1)
								{
									courseRegWaitingList = courseRegistrationWaitingService.getWaitingCourseByClassGroupId(
											semesterSubId, registerNumber, classGroupId);
								}
								model.addAttribute("courseRegWaitingList", courseRegWaitingList);
								callCourseRegistrationTypes(registrationOption, pageSize, page, searchType, searchVal, 
										session, model);				
								model.addAttribute("WaitingListStatus", WaitingListStatus);
								model.addAttribute("infoMessage", message);
								urlPage = "mainpages/CourseList :: section";
							}
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
		catch(Exception ex)
		{
			//logger.trace("Regno--> " + registerNumber + " |Message--> " + message + " |msg--> " + msg + " |registrationOption--> " + registrationOption + " |Exception--> "+ ex );
			//ex.printStackTrace();
			model.addAttribute("flag", 1);
			registrationLogService.addErrorLog(ex.toString(), RegErrorMethod+" CourseRegistrationFormController", 
					"processRegisterProjectCourse", registerNumber, IpAddress);
			registrationLogService.UpdateLogoutTimeStamp2(IpAddress,registerNumber);
			urlPage = "redirectpage";
			return urlPage;
		}
		model.addAttribute("info", msg);
		model.addAttribute("infoMessage", message);
		return urlPage;		
	}
	
	
	@PostMapping(value = "processRegisterCourse")
	public String processRegisterCourse(String ClassID, String courseId, String courseType, String courseCode, 
							String courseOption, String clashSlot, String epjSlotFlag, 
							@RequestParam(value = "pageSize", required = false) Integer pageSize, 
							@RequestParam(value = "page", required = false) Integer page,
							@RequestParam(value = "searchType", required = false) Integer searchType, 
							@RequestParam(value = "searchVal", required = false) String searchVal,
							@RequestParam(value = "subCourseOption", required = false) String subCourseOption, 
							@RequestParam(value = "subCourseType", required = false) String subCourseType,
							@RequestParam(value = "subCourseDate", required = false) String subCourseDate,
							Model model, String[] clArr, HttpSession session, HttpServletRequest request) 
	{
		String registerNumber = (String) session.getAttribute("RegisterNumber");
		String registrationOption = (String) session.getAttribute("registrationOption");
		String IpAddress = (String) session.getAttribute("IpAddress");
		String studentCategory = (String) session.getAttribute("studentCategory");
		Integer approvalStatus = (Integer) session.getAttribute("approvalStatus");
		List<String> courseRegWaitingList = new ArrayList<String>();
		String msg = null, classId1 = "", classId2 = "", classId3 = "", classId = "", infoMsg = "", urlPage = "";
		String labErpId = "", labAssoId = "", genericCourseType = "", evaluationType = "";
		String[] courseTypels = {}, classNbr = {}, regStatusArr = {};		
		String thyErpId = "", thyAssoId="", seatRegClassNbr = "", message = null;
		String pClassIdArr = "", pCompTypeArr = "", pRegStatus = "", pSubCrTypeArr = "";
		
		Integer updateStatus = 1, patternId = 0;
		int allowStatus = 2, emdPjtFlag = 1, seatRegFlg = 1, regTypeCount = 0, regCompType = 0;		
		int regStatusFlag = 2, regStatus = 0;
		Long labSlotId = (long) 0, thySlotId = (long) 0;
				
		try
		{
			String authStatus = (String) session.getAttribute("authStatus");
			int authCheckStatus = courseRegCommonFn.validateCourseAuthKey(authStatus, registerNumber, courseId, 1);
			int pageAuthStatus = 2;
			String pageAuthKey = "";
			pageAuthKey = (String) session.getAttribute("pageAuthKey");
			pageAuthStatus = courseRegCommonFn.validatePageAuthKey(pageAuthKey, registerNumber, 2);
			
			/*System.out.println("authCheckStatus: "+ authCheckStatus +" | registerNumber: "+ registerNumber
					+" | pageAuthStatus: "+ pageAuthStatus);*/
			
			if((authCheckStatus == 1) && (registerNumber!=null) && (pageAuthStatus == 1) )
			{
				session.setAttribute("pageAuthKey", courseRegCommonFn.generatePageAuthKey(registerNumber, 2));
				
				int studyStartYear = (int) session.getAttribute("StudyStartYear");
				Integer StudentGraduateYear = (Integer) session.getAttribute("StudentGraduateYear");
				Integer semesterId  = (Integer) session.getAttribute("SemesterId");
				Integer programGroupId = (Integer) session.getAttribute("ProgramGroupId");
				String ProgramGroupCode = (String) session.getAttribute("ProgramGroupCode");
				String ProgramSpecCode = (String) session.getAttribute("ProgramSpecCode");
				Integer programSpecId = (Integer) session.getAttribute("ProgramSpecId");
				String pOldRegisterNumber = (String) session.getAttribute("OldRegNo"); 
									
				String semesterSubId = (String) session.getAttribute("SemesterSubId");				
				List<String> clashslot = new ArrayList<String>();
				CourseCatalogModel ccm = new CourseCatalogModel();				
				CourseAllocationModel courseAllocationModel = new CourseAllocationModel();
				CourseAllocationModel courseAllocationModel2 = new CourseAllocationModel();
				String[] classGroupId = session.getAttribute("classGroupId").toString().split("/");
				
				@SuppressWarnings("unchecked")
				List<String> compCourseList = (List<String>) session.getAttribute("compulsoryCourseList");
				String costCentreCode = (String) session.getAttribute("costCentreCode");
				Integer WaitingListStatus=(Integer) session.getAttribute("waitingListStatus");
				Integer compulsoryCourseStatus = (Integer) session.getAttribute("compulsoryCourseStatus");
				Integer PEUEAllowStatus = (Integer) session.getAttribute("PEUEAllowStatus");
				Integer regularFlag = (Integer) session.getAttribute("regularFlag");
				Integer reRegFlag =  (Integer) session.getAttribute("reRegFlag");
				String registrationMethod = (String) session.getAttribute("registrationMethod");
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
							
				ccm = courseCatalogService.getOne(courseId);
				if (ccm != null)
				{
					genericCourseType = ccm.getGenericCourseType();
					evaluationType = ccm.getEvaluationType();
				}
				
				int compulsoryStatus = 2;
				List<String> courseTypeArr = semesterMasterService.getCourseTypeComponentByGenericType(genericCourseType);
				/*System.out.println("subCourseOption: "+ subCourseOption +" | subCourseType: "+ subCourseType 
										+" | subCourseDate: "+ subCourseDate +" | allowStatus: "+ allowStatus);*/
				
				switch (allowStatus)
				{
					case 1:
						for (String courseList : clArr) 
						{	
							switch(courseList)
							{
								case "ELA":
									courseTypels = ClassID.split(",");									
									classNbr = courseTypels[1].split("/");									
									classId2 = classNbr[1];
									break;
									
								case "EPJ":
									courseTypels = ClassID.split(",");									
									classNbr = courseTypels[2].split("/");									
									classId3 = classNbr[1];
									break;
									
								default:
									courseTypels = ClassID.split(",");
									classNbr = courseTypels[0].split("/");									
									classId1 = classNbr[1];
									break;
							}							
						}						
							
						for (String courseList : clArr) 
						{							
							courseAllocationModel = new CourseAllocationModel();
							courseAllocationModel2 = new CourseAllocationModel();							
							
							if (!courseList.equals("EPJ"))
							{
								switch(courseList)
								{
									case "ELA":
										courseAllocationModel = courseAllocationService.getOne(classId2);
										if (courseAllocationModel != null)
										{
											labErpId = courseAllocationModel.getErpId();
											labSlotId = courseAllocationModel.getSlotId();
											labAssoId = courseAllocationModel.getAssoClassId();
											patternId = courseAllocationModel.getTimeTableModel().getPatternId();
										}
											
										switch(genericCourseType)
										{
											case "ETLP":
											case "ELP":
											courseAllocationModel2 = courseAllocationService.getCourseAllocationCourseIdTypeEmpidSlotAssoList(semesterSubId,
																			classGroupId, classType, courseId, "EPJ", labErpId, labSlotId, labAssoId, 
																			ProgramGroupCode, ProgramSpecCode, costCentreCode);
											if (courseAllocationModel2!=null)
											{
												classId3 = courseAllocationModel2.getClassId();
											}
											else
											{
												emdPjtFlag = 2;
											}
											break;
										}
										break;
												
									default:
										courseAllocationModel = courseAllocationService.getOne(classId1);
										if (courseAllocationModel != null)
										{
											thyErpId = courseAllocationModel.getErpId();
											thySlotId = courseAllocationModel.getSlotId();
											thyAssoId = courseAllocationModel.getAssoClassId();	
											patternId = courseAllocationModel.getTimeTableModel().getPatternId();
										}
													
										switch(genericCourseType)
										{
											case "ETP": 
												if (courseList.equals("ETH"))
												{
													courseAllocationModel2 = courseAllocationService.getCourseAllocationCourseIdTypeEmpidSlotAssoList(semesterSubId,
																					classGroupId, classType, courseId, "EPJ", thyErpId, thySlotId, thyAssoId, 
																					ProgramGroupCode, ProgramSpecCode, costCentreCode);
															
													if (courseAllocationModel2!=null)
													{
														classId3 = courseAllocationModel2.getClassId();
													}
													else
													{
														emdPjtFlag = 2;
													}
												}												
												break;
										}
										break;
								}									
							}
															
							if ((!courseList.equals("EPJ")) && (!courseList.equals("PJT")) 
									&& (!courseAllocationModel.getTimeTableModel().getClashSlot().equals("NONE")))
							{
								clashslot.add(courseAllocationModel.getTimeTableModel().getClashSlot());
							}								
						}
							
						//Get the Registration Status
						/*switch(courseOption)
						{
							case "RGR":
							case "AUD":
							case "RGCE":
							case "RPEUE":
								regStatus = 10;
							break;
							default:
								regStatus = 1;
							break;
						}*/
						regStatus = courseRegCommonFn.getRegistrationStatus(approvalStatus, courseOption, 
											genericCourseType, evaluationType, studentCategory);
							
							
						//regStatusArr = courseRegCommonFn.checkClash(clashslot,semesterSubId,registerNumber,"ADD","").split("/");
						regStatusArr = courseRegCommonFn.checkClash(patternId, clashslot, semesterSubId, registerNumber, "ADD", "").split("/");
						regStatusFlag = Integer.parseInt(regStatusArr[0]);
						//System.out.println("regStatusFlag -- > " + regStatusFlag + " |Message--> " + regStatusArr[1]);
													
						if (regStatusFlag == 2)
						{
							message = regStatusArr[1];
						}
						else
						{								
							for (String courseList : clArr) 
							{							
								switch(courseList)
								{	
									case "ELA":
										seatRegClassNbr = classId2;										
										message =  "Lab Component Seats not available";
										break;
									case "EPJ":
										seatRegClassNbr = classId3;											
										message = "Project Component Seats not available";
										break;
									default:
										seatRegClassNbr = classId1;											
										switch(courseList)
										{
											case "ETH":
											case "TH":
												message = "Theory Component Seats not available";
											break;
											case "SS":
												message = "Softskills Component Seats not available";
											break;
											case "LO":
												message = "Lab Component Seats not available";
											break;
										}
									break;
									}
								
									if (courseAllocationService.getAvailableRegisteredSeats(seatRegClassNbr) <= 0) 
									{
										seatRegFlg = 2;
										break;
									}									
								}								
								
								if ((emdPjtFlag == 1) && (seatRegFlg == 2))
								{
									model.addAttribute("infoMessage", message);
								}
								else
								{
									message = null;
								}						
									
								if ((regStatusFlag == 1) && (seatRegFlg == 1) && (emdPjtFlag == 1))
								{
									regTypeCount = 0;
									
									for (String courseList : clArr) 
									{
										switch(courseList)
										{
											case "ELA":
												classId = classId2;
												break;
											case "EPJ":
												classId = classId3;
												break;
											default:
												classId = classId1;
												break;
										}
										
										if (pCompTypeArr.equals(""))
										{
											pClassIdArr = classId;
											pCompTypeArr = courseList;
										}
										else
										{
											pClassIdArr = pClassIdArr +"|"+ classId;
											pCompTypeArr = pCompTypeArr +"|"+ courseList;
										}
										
										regTypeCount = regTypeCount + 1;										
									}
									
									if ((!subCourseOption.equals("")) && (!subCourseOption.equals(null)))
									{
										if (genericCourseType.equals("ECA") && courseOption.equals("CS"))
										{
											for (String e: subCourseType.split(","))
											{
												if (pSubCrTypeArr.equals(""))
												{
													pSubCrTypeArr = e;
												}
												else
												{
													pSubCrTypeArr = pSubCrTypeArr +"|"+ e;
												}
											}
											break;
										}
										else
										{
											switch(courseOption)
											{
												case "RR":
												case "RRCE":
												case "GI":
												case "GICE":
												case "RGCE":
												case "RPCE":
												case "RWCE":
													for (String e: subCourseType.split(","))
													{
														if (pSubCrTypeArr.equals(""))
														{
															pSubCrTypeArr = e;
														}
														else
														{
															pSubCrTypeArr = pSubCrTypeArr +"|"+ e;
														}
													}
													break;
												case "CS":
													String[] subCrsOptArr = subCourseOption.split("/");
													subCourseOption = subCrsOptArr[0];
													subCourseType = subCrsOptArr[1];
													subCourseDate = subCrsOptArr[2];
													for (@SuppressWarnings("unused") String courseList : clArr) 
													{
														if (pSubCrTypeArr.equals(""))
														{
															pSubCrTypeArr = subCourseType;
														}
														else
														{
															pSubCrTypeArr = pSubCrTypeArr +"|"+ subCourseType;
														}
													}
													break;
												case "MIN":
												case "HON":
													pSubCrTypeArr = pCompTypeArr;
													break;
												default:
													subCourseType = "";
													subCourseDate = "";
													break;
											}
										}
									}
									
									if (regTypeCount != courseTypeArr.size())
									{
										regCompType = 1;
									}
									
									/*System.out.println("semesterSubId: "+ semesterSubId +" | pClassIdArr: "+ pClassIdArr 
											+" | registerNumber: "+ registerNumber +" | courseId: "+ courseId 
											+" | pCompTypeArr: "+ pCompTypeArr +" | courseOption: "+ courseOption 
											+" | regStatus: "+ regStatus +" | regCompType: "+ regCompType 
											+" | registerNumber: "+ registerNumber +" | IpAddress: "+ IpAddress 
											+" | subCourseOption: "+ subCourseOption +" | pSubCrTypeArr: "+ pSubCrTypeArr 
											+" | subCourseDate: "+ subCourseDate);*/
									
									pRegStatus = courseRegistrationService.courseRegistrationAdd2(semesterSubId, pClassIdArr, 
													registerNumber, courseId, pCompTypeArr, courseOption, regStatus, regCompType, 
													registerNumber, IpAddress, "GEN", subCourseOption, "INSERT", pSubCrTypeArr, 
													subCourseDate);
									if (pRegStatus.equals("SUCCESS"))
									{
										msg = "Selected Course Successfully Registered";
									}
									else if ((pRegStatus.equals("FAIL")) || (pRegStatus.substring(0, 5).equals("error")))
									{
										message = "Technical error.";
										registrationLogService.addErrorLog(pRegStatus.toString()+"<-CODE->"+courseId, RegErrorMethod+"CourseRegistrationFormController", 
												"processRegisterCourseInsertPROC", registerNumber, IpAddress);
										registrationLogService.UpdateLogoutTimeStamp2(IpAddress,registerNumber);
									}
									else
									{
										message = pRegStatus;
									}
								}							
							}						
							
							session.setAttribute("authStatus", "NONE");
														
							
							if (compulsoryCourseStatus == 1)
							{
								compulsoryStatus = courseRegCommonFn.compulsoryCourseCheck(programGroupId, studyStartYear, 
														StudentGraduateYear, semesterId, semesterSubId, registerNumber, 
														classGroupId, classType, ProgramSpecCode, programSpecId, 
														ProgramGroupCode, pOldRegisterNumber, compCourseList, 
														costCentreCode);
								session.setAttribute("compulsoryCourseStatus", compulsoryStatus);
							}
							
							if (compulsoryStatus == 1)
							{
								getCompulsoryCourseList(registrationOption, pageSize, page, searchType, searchVal, 
										subCourseOption, session, model, compCourseList);
								model.addAttribute("info", msg);
								urlPage = "mainpages/CompulsoryCourseList :: section";
							}
							else
							{
								if (registrationOption.equals("COMP"))
								{
									session.removeAttribute("registrationOption");
									
									//model.addAttribute("regularFlag", session.getAttribute("regularFlag"));
									//model.addAttribute("PEUEAllowStatus", session.getAttribute("PEUEAllowStatus"));
									//model.addAttribute("registrationMethod", session.getAttribute("registrationMethod"));
									
									model.addAttribute("regOptionList", courseRegCommonFn.getRegistrationOption(ProgramGroupCode, 
											registrationMethod, regularFlag, reRegFlag, PEUEAllowStatus, programSpecId, studyStartYear, 
											curriculumVersion));
									
									model.addAttribute("studySystem", session.getAttribute("StudySystem"));
									model.addAttribute("showFlag", 0);
									model.addAttribute("info", msg);
									
									urlPage = "mainpages/RegistrationOptionList :: section";
								}
								else
								{
									callCourseRegistrationTypes(registrationOption, pageSize, page, searchType, searchVal, session, model);
									if(WaitingListStatus==1)
									{
										courseRegWaitingList = courseRegistrationWaitingService.getWaitingCourseByClassGroupId(
												semesterSubId, registerNumber, classGroupId);
									}
									model.addAttribute("courseRegWaitingList", courseRegWaitingList);
									model.addAttribute("WaitingListStatus", WaitingListStatus);
									model.addAttribute("info", msg);
									urlPage = "mainpages/CourseList :: section";
								}
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
		catch(Exception ex)
		{
			/*logger.error(ex);
			logger.trace("Regno: "+ registerNumber +" | Message: "+ message 
					+ " | msg: " + msg + " | pClassIdArr: " + pClassIdArr 
					+ " | pCompTypeArr: "+ pCompTypeArr +" | registrationOption: " + registrationOption);*/
						
			session.setAttribute("authStatus", "NONE");
			model.addAttribute("flag", 1);
			
			registrationLogService.addErrorLog(ex.toString(), RegErrorMethod+"CourseRegistrationFormController", 
					"processRegisterCourse", registerNumber, IpAddress);
			registrationLogService.UpdateLogoutTimeStamp2(IpAddress,registerNumber);
			
			urlPage = "redirectpage";
			return urlPage;
		}
				
		model.addAttribute("infoMessage", message);
		return urlPage;		
	}
	

	@PostMapping("processSearch")
	public String processSearch(Model model, HttpSession session, 
						@RequestParam(value = "pageSize", required = false) Integer pageSize,
						@RequestParam(value = "page", required = false) Integer page, 
						@RequestParam(value = "searchType", required = false) Integer searchType,
						@RequestParam(value = "searchVal", required = false) String searchVal, 
						@RequestParam(value = "subCourseOption", required = false) String subCourseOption, 
						HttpServletRequest request) 
	{	
		String registerNumber = (String) session.getAttribute("RegisterNumber");
		String IpAddress = (String) session.getAttribute("IpAddress");
		
		String msg = null, infoMsg = "", urlPage = "";
		Integer updateStatus = 1;
		int allowStatus = 2;
		int pageAuthStatus = 2;
		String pageAuthKey = "";
		pageAuthKey = (String) session.getAttribute("pageAuthKey");
		pageAuthStatus = courseRegCommonFn.validatePageAuthKey(pageAuthKey, registerNumber, 2);
		List<String> courseRegWaitingList = new ArrayList<String>();
		Integer WaitingListStatus=(Integer) session.getAttribute("waitingListStatus");
		
		
		try 
		{
			if ((registerNumber!=null) && (pageAuthStatus == 1))
			{	
				String registrationOption = (String) session.getAttribute("registrationOption");				
				String semesterSubId = (String) session.getAttribute("SemesterSubId");
				String[] classGroupId = session.getAttribute("classGroupId").toString().split("/");
				Date startDate = (Date) session.getAttribute("startDate");
				Date endDate = (Date) session.getAttribute("endDate");
				String startTime = (String) session.getAttribute("startTime");
				String endTime = (String) session.getAttribute("endTime");
				
				String returnVal = courseRegCommonFn.AddorDropDateTimeCheck(startDate, endDate, startTime, endTime, 
										registerNumber, updateStatus, IpAddress);
				String[] statusMsg = returnVal.split("/");
				allowStatus = Integer.parseInt(statusMsg[0]);
				infoMsg = statusMsg[1];
					
				switch (allowStatus)
				{
					case 1:
						callCourseRegistrationTypes(registrationOption, pageSize, page, searchType, searchVal, 
								session, model);
						if (WaitingListStatus == 1)
						{
							courseRegWaitingList = courseRegistrationWaitingService.getWaitingCourseByClassGroupId(
									semesterSubId, registerNumber, classGroupId);
						}
						model.addAttribute("courseRegWaitingList", courseRegWaitingList);
						model.addAttribute("registrationOption", registrationOption);
						model.addAttribute("searchFlag", 1);
						urlPage = "mainpages/CourseList::section";						
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
		catch (Exception ex) 
		{
			model.addAttribute("flag", 1);
			registrationLogService.addErrorLog(ex.toString(), RegErrorMethod+"CourseRegistrationFormController", 
					"processSearch", registerNumber, IpAddress);
			registrationLogService.UpdateLogoutTimeStamp2(IpAddress,registerNumber);
			urlPage = "redirectpage";			
			return urlPage;
		}
		return urlPage; 
	}
	
	@PostMapping(value="viewCorrespondingCourse")
	public String viewCorrespondingCourse(String courseId, String erpId, String genericCourseType, String classId, 
						@RequestParam(value = "page", required = false) Integer page,
						@RequestParam(value = "searchType", required = false) Integer searchType,
						@RequestParam(value = "searchVal", required = false) String searchVal, 
						Model model, HttpSession session, HttpServletRequest request)//M2 
	{
		String registerNumber = (String) session.getAttribute("RegisterNumber");
		String semesterSubId = (String) session.getAttribute("SemesterSubId");
		String IpAddress = (String) session.getAttribute("IpAddress");
		Integer WaitingListStatus=(Integer) session.getAttribute("waitingListStatus");
		
		int ethExistFlag = 2, epjExistFlag = 2, epjSlotFlag = 2;
		int regStatusFlag = 2, regAllowFlag = 1, wlAllowFlag = 1, audAllowFlag = 1, rgrAllowFlag = 2, minAllowFlag = 2, 
				honAllowFlag = 2, adlAllowFlag = 2, RPEUEAllowFlag = 2, csAllowFlag = 2, RUCUEAllowFlag = 2;
		Integer allowStatus = 2, updateStatus = 1;
				
		String urlPage = "", courseTypeDisplay = "", msg = null, message = null, courseOption = "", infoMsg = "";
		String courseCategory = "NONE", courseCode = "", genericCourseTypeDisplay = "", ccCourseId = "";		
		String[] regStatusArr = {};
		
		List<String> courseTypeArr = new ArrayList<String>();
		CourseCatalogModel courseCatalog = new CourseCatalogModel();
				
		try
		{
			if (semesterSubId != null)
			{
			  String authStatus = (String) session.getAttribute("authStatus");
			  String corAuthStatus = (String) session.getAttribute("corAuthStatus");
			  int authCheckStatus = courseRegCommonFn.validateCourseAuthKey(authStatus, registerNumber, courseId, 1);
			  int pageAuthStatus = 2;
			  String pageAuthKey = "";
			  pageAuthKey = (String) session.getAttribute("pageAuthKey");
			  pageAuthStatus = courseRegCommonFn.validatePageAuthKey(pageAuthKey, registerNumber, 2);
			  
			  if((authCheckStatus == 1) && (pageAuthStatus == 1))
			  {	
				session.setAttribute("pageAuthKey", courseRegCommonFn.generatePageAuthKey(registerNumber, 2));
				Integer pProgramGroupId = (Integer) session.getAttribute("ProgramGroupId");
				String pProgramGroupCode = (String) session.getAttribute("ProgramGroupCode"); 
				Integer pProgramSpecId = (Integer) session.getAttribute("ProgramSpecId");
				Integer regularFlag = (Integer) session.getAttribute("regularFlag");
				//String pProgramSpecCode = (String) session.getAttribute("ProgramSpecCode");
				String studentStudySystem = (String) session.getAttribute("studentStudySystem");
				Float curriculumVersion = (Float) session.getAttribute("curriculumVersion");
				
				String registrationOption = (String) session.getAttribute("registrationOption");
				String subCourseOption = (String) session.getAttribute("subCourseOption");
				Integer StudyStartYear = (Integer) session.getAttribute("StudyStartYear");
				//String costCentreCode = (String) session.getAttribute("costCentreCode");
				
				@SuppressWarnings("unchecked")
				List<String> registerNumberList = (List<String>) session.getAttribute("registerNumberList");
				@SuppressWarnings("unchecked")
				List<String> compCourseList = (List<String>) session.getAttribute("compulsoryCourseList");
															
				//String[] classGroupId = session.getAttribute("classGroupId").toString().split("/");
				Date startDate = (Date) session.getAttribute("startDate");
				Date endDate = (Date) session.getAttribute("endDate");
				String startTime = (String) session.getAttribute("startTime");
				String endTime = (String) session.getAttribute("endTime");
				
				String returnVal = courseRegCommonFn.AddorDropDateTimeCheck(startDate, endDate, startTime, endTime, 
										registerNumber, updateStatus, IpAddress);
				String[] statusMsg = returnVal.split("/");
				allowStatus = Integer.parseInt(statusMsg[0]);
				infoMsg = statusMsg[1];
				
				String subCourseType = "", subCourseDate = "";
				//List<CourseAllocationModel> list1 = new ArrayList<CourseAllocationModel>();
				//List<CourseAllocationModel> ela = new ArrayList<CourseAllocationModel>();
				//List<CourseAllocationModel> epj = new ArrayList<CourseAllocationModel>();
				List<CourseAllocationModel> courseAllocationList = new ArrayList<CourseAllocationModel>();
				
				@SuppressWarnings("unchecked")
				List<CourseAllocationModel> list1 = ((session.getAttribute("camList") != null) && (!session.getAttribute("camList").equals(""))) 
														? (List<CourseAllocationModel>) session.getAttribute("camList") 
																: new ArrayList<CourseAllocationModel>();
				
				@SuppressWarnings("unchecked")
				List<CourseAllocationModel> ela = ((session.getAttribute("camList2") != null) && (!session.getAttribute("camList2").equals(""))) 
														? (List<CourseAllocationModel>) session.getAttribute("camList2") 
																: new ArrayList<CourseAllocationModel>();
				
				@SuppressWarnings("unchecked")
				List<CourseAllocationModel> epj = ((session.getAttribute("camList3") != null) && (!session.getAttribute("camList3").equals("")))
														? (List<CourseAllocationModel>) session.getAttribute("camList3") 
																: new ArrayList<CourseAllocationModel>();
				
				
				courseCatalog = courseCatalogService.getOne(courseId);
				if (courseCatalog != null)
				{
					courseCode = courseCatalog.getCode();
					genericCourseTypeDisplay = courseCatalog.getCourseTypeComponentModel().getDescription();
				}
				
				switch(allowStatus)
				{
					case 1:						
						regStatusArr = corAuthStatus.split("/");
						regStatusFlag = authCheckStatus;
						courseOption = regStatusArr[0];
						regAllowFlag = Integer.parseInt(regStatusArr[1]);
						wlAllowFlag = Integer.parseInt(regStatusArr[2]);
						audAllowFlag = Integer.parseInt(regStatusArr[3]);
						rgrAllowFlag= Integer.parseInt(regStatusArr[4]);
						minAllowFlag = Integer.parseInt(regStatusArr[5]);
						honAllowFlag = Integer.parseInt(regStatusArr[6]);
						courseCategory = regStatusArr[7];
						adlAllowFlag = Integer.parseInt(regStatusArr[8]);
						RPEUEAllowFlag = Integer.parseInt(regStatusArr[9]);
						csAllowFlag = Integer.parseInt(regStatusArr[14]);
						RUCUEAllowFlag = Integer.parseInt(regStatusArr[15]);
						ccCourseId = regStatusArr[16];
																							
						/*Integer PEUEStatusFlag=courseRegCommonFn.ValidateCourseRegistration(authKeyVal,registerNumber,courseId);
						session.setAttribute("PEUEStatusFlag", PEUEStatusFlag);
					
						if(PEUEStatusFlag == 1)
						{*/
							switch(courseOption)
							{
								case "RR":
								case "RRCE":
									/*if (!regStatusArr[6].equals("NONE"))
									{
										courseTypeArr = Arrays.asList(regStatusArr[6].split(","));
									}*/
									
									if (!regStatusArr[10].equals("NONE"))
									{
										courseTypeArr = Arrays.asList(regStatusArr[10].split(","));
									}
									
									if (courseTypeArr.size() <= 0)
									{
										courseTypeArr = semesterMasterService.getCourseTypeComponentByGenericType(genericCourseType);
									}									
									break;
								
								default:
									courseTypeArr = semesterMasterService.getCourseTypeComponentByGenericType(genericCourseType);
									break;
							}
								
							/*switch(courseOption)
							{
								case "RGR":
									switch(registrationOption)
									{
										case "HON":
										case "MIN":
											courseOption = registrationOption;
											break;
									}
									break;
							}*/
								
							switch(courseOption)
							{
								case "RR":
								case "RRCE":
								case "GI":
								case "GICE":
								case "RGCE":
								case "RPCE":
								case "RWCE":
									/*subCourseOption = regStatusArr[7];
									subCourseType = regStatusArr[9];
									subCourseDate = regStatusArr[10];*/
									
									subCourseOption = regStatusArr[11];
									subCourseType = regStatusArr[12];
									subCourseDate = regStatusArr[13];
									
									break;
									
								default:
									/*if (regStatusArr[7].equals("NONE"))
									{
										subCourseOption = "";
									}*/
									
									if (regStatusArr[11].equals("NONE"))
									{
										subCourseOption = "";
									}
									
									break;
							}
													
							for (String crstp: courseTypeArr) 
							{
								if (courseTypeDisplay.equals(""))
								{
									courseTypeDisplay = semesterMasterService.getCourseTypeMasterByCourseType(crstp).getDescription();
								}
								else
								{
									courseTypeDisplay = courseTypeDisplay +" / "+ semesterMasterService.getCourseTypeMasterByCourseType(crstp).getDescription();
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
								
							if ((courseTypeArr.size() == 2) && (genericCourseType.equals("ETLP")) 
									&& (ethExistFlag == 1) && (epjExistFlag == 1))
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
											switch(crtp)
											{
												case "EPJ":
													//epj = courseAllocationService.getCourseAllocationCourseIdTypeList(semesterSubId, 
													//			classGroupId, classType, courseId, "EPJ", pProgramGroupCode, 
													//			pProgramSpecCode, costCentreCode);
													model.addAttribute("cam3", epj);
													break;
													
												case "ELA":
													switch(genericCourseType)
													{
														case "ETLP":
														case "ETL":
															//ela = courseAllocationService.getCourseAllocationCourseIdTypeEmpidAssocList(
															//			semesterSubId, classGroupId, classType, courseId, "ELA", erpId, 
															//			pProgramGroupCode, pProgramSpecCode, costCentreCode);
															//model.addAttribute("cam2", ela);
															model.addAttribute("cam2", courseAllocationService.getAllocationByEmployeeId(ela, erpId));
															break;
															
														default:
															//ela = courseAllocationService.getCourseAllocationCourseIdTypeList(semesterSubId, 
															//			classGroupId, classType, courseId, "ELA", pProgramGroupCode, 
															//			pProgramSpecCode, costCentreCode);
															model.addAttribute("cam2", ela);
															break;
													}													
													break;
													
												default:
													//list1 = courseAllocationService.getCourseAllocationCourseIdTypeList(semesterSubId, 
													//			classGroupId, classType, courseId, crtp, pProgramGroupCode, pProgramSpecCode, 
													//			costCentreCode);
													model.addAttribute("cam", list1);
													break;
											}																		
										}
									}
									
									//Assigning the all course type of allocation list to one course allocation list									
									if (!list1.isEmpty())
									{
										courseAllocationList.addAll(list1);
									}
									if (!ela.isEmpty())
									{
										courseAllocationList.addAll(ela);
									}
																		
									model.addAttribute("shcssList", studentHistoryService.getStudentHistoryCS2(registerNumberList, 
											courseCode, studentStudySystem, pProgramSpecId, StudyStartYear, curriculumVersion, 
											semesterSubId, courseCategory, courseOption, ccCourseId));
									model.addAttribute("minorList", semesterMasterService.getAdditionalLearningTitleByLearnTypeGroupIdSpecIdAndCourseCode(
												minAllowFlag, "MIN", pProgramGroupId, pProgramSpecId, courseCode, studentStudySystem));
									model.addAttribute("honorList", semesterMasterService.getAdditionalLearningTitleByLearnTypeGroupIdSpecIdAndCourseCode(
												honAllowFlag, "HON", pProgramGroupId, pProgramSpecId, courseCode, studentStudySystem));
									model.addAttribute("courseOptionList",semesterMasterService.getRegistrationCourseOption(
											courseOption, genericCourseType, rgrAllowFlag, audAllowFlag, honAllowFlag, 
											minAllowFlag, adlAllowFlag, csAllowFlag, RPEUEAllowFlag, RUCUEAllowFlag));
									
									//model.addAttribute("tlClashMapList", courseRegCommonFn.getClashSlotStatus(
									//		semesterSubId, registerNumber, courseAllocationList));
									callSlotInformation(model, semesterSubId, registerNumber, courseAllocationList);
									
									model.addAttribute("regAllowFlag", regAllowFlag);
									model.addAttribute("regularFlag", regularFlag);
									model.addAttribute("wlAllowFlag", wlAllowFlag);
									model.addAttribute("rgrAllowFlag", rgrAllowFlag);
									model.addAttribute("minAllowFlag", minAllowFlag);
									model.addAttribute("honAllowFlag", honAllowFlag);
									model.addAttribute("RPEUEAllowFlag", RPEUEAllowFlag);
									model.addAttribute("csAllowFlag", csAllowFlag);
									model.addAttribute("RUCUEAllowFlag", RUCUEAllowFlag);
									model.addAttribute("courseCatalogModel", courseCatalog);
									model.addAttribute("epjSlotFlag", epjSlotFlag);
									model.addAttribute("page", page);
									model.addAttribute("srhType", searchType);
									model.addAttribute("srhVal", searchVal);
									model.addAttribute("courseOption", courseOption);
									model.addAttribute("registrationOption", registrationOption);
									model.addAttribute("audAllowFlag", audAllowFlag);
									model.addAttribute("adlAllowFlag", adlAllowFlag);
									model.addAttribute("WaitingListStatus", WaitingListStatus);
									model.addAttribute("subCourseOption", subCourseOption);
									model.addAttribute("tlcourseType", courseTypeArr);					
									model.addAttribute("courseTypeDisplay", courseTypeDisplay);
									model.addAttribute("genericCourseTypeDisplay", genericCourseTypeDisplay);
									model.addAttribute("ProgramGroupCode", pProgramGroupCode);
									model.addAttribute("tlClassId", classId);
									model.addAttribute("subCourseType", subCourseType);
									model.addAttribute("subCourseDate", subCourseDate);
									model.addAttribute("tlCourseCategory", courseCategory);
									model.addAttribute("tlCompCourseList", compCourseList);
									
									//System.out.println("corAuthStatus: "+ corAuthStatus);
									urlPage = "mainpages/CourseRegistration :: section";
									
									break;
								
								case 2:
									model.addAttribute("infoMessage", message);
									urlPage = processRegistrationOption(registrationOption, model, session, 5, page, searchType, 
													searchVal, subCourseOption, request);							 
									break;  
							}							
							break;						
						/*}
						else
						{
							session.removeAttribute("registrationOption");
							model.addAttribute("studySystem", studentStudySystem);
							model.addAttribute("maxCredit", maxCredit);
							model.addAttribute("registrationMethod", (String)session.getAttribute("registrationMethod"));
							model.addAttribute("ProgramGroupCode", (String) session.getAttribute("ProgramGroupCode"));							
							model.addAttribute("showFlag", 0);
							urlPage = "mainpages/RegistrationOptionList::section";
						}*/
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
			else
			{
				model.addAttribute("flag", 1);
				urlPage = "redirectpage";
				return urlPage;
			}
		}		
		catch(Exception ex)
		{
			model.addAttribute("flag", 1);
			registrationLogService.addErrorLog(ex.toString(), RegErrorMethod+"CourseRegistrationFormController", 
					"viewCorrespondingCourse", registerNumber, IpAddress);
			registrationLogService.UpdateLogoutTimeStamp2(IpAddress,registerNumber);
			urlPage = "redirectpage";
			return urlPage;
		}
		return urlPage;
	}
	
	
	@PostMapping(value = "processWaitingCourse")
	public String processWaitingCourse(String ClassID, String courseId, String courseType, String courseCode, 
						String courseOption, String clashSlot, String epjSlotFlag,  
						@RequestParam(value = "pageSize", required = false) Integer pageSize, 
						@RequestParam(value = "page", required = false) Integer page,
						@RequestParam(value = "searchType", required = false) Integer searchType, 
						@RequestParam(value = "searchVal", required = false) String searchVal,
						@RequestParam(value = "subCourseOption", required = false) String subCourseOption, 
						@RequestParam(value = "subCourseType", required = false) String subCourseType,
						@RequestParam(value = "subCourseDate", required = false) String subCourseDate,
						String[] clArr, Model model, HttpSession session, HttpServletRequest request) 
	{
		String registerNumber = (String) session.getAttribute("RegisterNumber");				
		Integer updateStatus = 1, regTypeCount = 0, regCompType = 0, patternId = 0;
		int allowStatus = 2,regStatus = 0;		
		String IpAddress=(String) session.getAttribute("IpAddress");
		Integer WaitingListStatus=(Integer) session.getAttribute("waitingListStatus");
		List<String> courseRegWaitingList = new ArrayList<String>();
		String msg = null, classStatus = "", classId1 = "", classId2 = "", classId3 = "", classId = "", infoMsg = "";
		String labErpId = "", labAssoId = "", genericCourseType = "",evaluationType="NONE";
		Long labSlotId = (long) 0;
		String urlPage = "", seatWaitClassNbr = "";
		String[] courseTypels = {}, classNbr = {}, regStatusArr = {};
		@SuppressWarnings("unused")
		int emdPjtFlag = 1, regFlag = 0;
		Integer waitFlag = 0;	
		String thyErpId = "", thyAssoId="", message = null;
		Long thySlotId = (long) 0;		
		int wlCount = 0;
		int regStatusFlag = 2, seatWaitFlg = 2, wlCountFlg = 2;
		String authStatus = (String) session.getAttribute("authStatus");
		int authCheckStatus = courseRegCommonFn.validateCourseAuthKey(authStatus, registerNumber, courseId, 1);
		int pageAuthStatus = 2;
		String pageAuthKey = "";
		pageAuthKey = (String) session.getAttribute("pageAuthKey");
		pageAuthStatus = courseRegCommonFn.validatePageAuthKey(pageAuthKey, registerNumber, 2);
		
		try
		{
			if((authCheckStatus ==1) && (registerNumber!=null) 
					&& (pageAuthStatus==1) && (WaitingListStatus == 1))
			{
				session.setAttribute("pageAuthKey", courseRegCommonFn.generatePageAuthKey(registerNumber, 2));
				String registrationOption = (String) session.getAttribute("registrationOption");								
				String semesterSubId = (String) session.getAttribute("SemesterSubId");				
				List<String> clashslot = new ArrayList<String>();
				CourseCatalogModel ccm = new CourseCatalogModel();
				String studentCategory = (String) session.getAttribute("studentCategory");
				Integer approvalStatus = (Integer) session.getAttribute("approvalStatus");
				String ProgramGroupCode = (String) session.getAttribute("ProgramGroupCode");
				String ProgramSpecCode = (String) session.getAttribute("ProgramSpecCode");
				String costCentreCode = (String) session.getAttribute("costCentreCode");
				
				CourseAllocationModel courseAllocationModel = new CourseAllocationModel();
				CourseAllocationModel courseAllocationModel2 = new CourseAllocationModel();
				String[] classGroupId = session.getAttribute("classGroupId").toString().split("/");
									
				Date startDate = (Date) session.getAttribute("startDate");
				Date endDate = (Date) session.getAttribute("endDate");
				String startTime = (String) session.getAttribute("startTime");
				String endTime = (String) session.getAttribute("endTime");
				String returnVal = courseRegCommonFn.AddorDropDateTimeCheck(startDate, endDate, startTime, endTime, 
										registerNumber, updateStatus, IpAddress);
					
				String[] statusMsg = returnVal.split("/");
				allowStatus = Integer.parseInt(statusMsg[0]);
				infoMsg = statusMsg[1];
							
				ccm = courseCatalogService.getOne(courseId);
				genericCourseType = ccm.getGenericCourseType();
				evaluationType=ccm.getEvaluationType();
				List<String> courseTypeArr = semesterMasterService.getCourseTypeComponentByGenericType(genericCourseType);
					
				switch(allowStatus)
				{
					case 1:
						for (String courseList : clArr) 
						{	
							switch(courseList)
							{
								case "ELA":
									courseTypels = ClassID.split(",");
									classNbr = courseTypels[1].split("/");
									classStatus = classNbr[0];
									classId2 = classNbr[1];
									
									switch(waitFlag)	
									{
										default:
										switch(classStatus)
										{
											case "GEN":
												regFlag = 1;
												break;
											case "WL":
												waitFlag = 1;
												regFlag = 0;
												break;
										}
										break;
									}
									break;
									
								case "EPJ":
									courseTypels = ClassID.split(",");
									classId3 = courseTypels[2];
									break;
									
									default:
									courseTypels = ClassID.split(",");
									classNbr = courseTypels[0].split("/");
									classStatus = classNbr[0];
									classId1 = classNbr[1];
										
									switch(waitFlag)
									{
										default:
											switch(classStatus)
											{
												case "GEN":
													regFlag = 1;
													break;
												case "WL":
													waitFlag = 1;
													regFlag = 0;
													break;												
											}
									}
									break;
								}							
							}
							
							for (String courseList : clArr) 
							{							
								courseAllocationModel = new CourseAllocationModel();
								courseAllocationModel2 = new CourseAllocationModel();							
								
								if (!courseList.equals("EPJ"))
								{
									switch(courseList)
									{
										case "ELA":
											courseAllocationModel = courseAllocationService.getOne(classId2);
											if (courseAllocationModel != null)
											{
												labErpId = courseAllocationModel.getErpId();
												labSlotId = courseAllocationModel.getSlotId();
												labAssoId = courseAllocationModel.getAssoClassId();
												patternId = courseAllocationModel.getTimeTableModel().getPatternId();
											}
											
											switch(genericCourseType)
											{
												case "ETLP":
												case "ELP":
													courseAllocationModel2 = courseAllocationService.getCourseAllocationCourseIdTypeEmpidSlotAssoList(
																				semesterSubId, classGroupId, classType, courseId, "EPJ", labErpId, 
																				labSlotId, labAssoId, ProgramGroupCode, ProgramSpecCode, costCentreCode);
													
													if (courseAllocationModel2!=null)
													{
														classId3 = courseAllocationModel2.getClassId();
													}
													else
													{
														emdPjtFlag = 2;
													}
													break;
											}
											break;
												
										default:
											courseAllocationModel = courseAllocationService.getOne(classId1);
											if (courseAllocationModel != null)
											{
												thyErpId = courseAllocationModel.getErpId();
												thySlotId = courseAllocationModel.getSlotId();
												thyAssoId = courseAllocationModel.getAssoClassId();
												patternId = courseAllocationModel.getTimeTableModel().getPatternId();
											}
													
											switch(genericCourseType)
											{
												case "ETP": 
													if (courseList.equals("ETH"))
													{
														courseAllocationModel2 = courseAllocationService.getCourseAllocationCourseIdTypeEmpidSlotAssoList(
																					semesterSubId, classGroupId, classType, courseId, "EPJ", thyErpId, 
																					thySlotId, thyAssoId, ProgramGroupCode, ProgramSpecCode, costCentreCode);
														
														if (courseAllocationModel2!=null)
														{
															classId3 = courseAllocationModel2.getClassId();
														}
														else
														{
															emdPjtFlag = 2;
														}
													}												
												break;
											}
											break;
									}
										
								}
								
								if ((!courseList.equals("EPJ")) && (!courseList.equals("PJT")))
								{
									clashslot.add(courseAllocationModel.getTimeTableModel().getClashSlot());
								}								
							}
							
							switch(courseOption)
							{
								case "RGR":
								case "AUD":
								case "RGCE":
								case "RPEUE":
									regStatus = 10;
									break;
								default:
									regStatus = 1;
									break;
							}
							regStatus = courseRegCommonFn.getRegistrationStatus(approvalStatus, courseOption, 
									genericCourseType, evaluationType, studentCategory);
							
							switch(courseOption)
							{
								case "RRCE":
								case "CS":
								case "MIN":
								case "HON":
									@SuppressWarnings("unused") 
									int subRegFlag = 1;
									break;
							}
							
							//regStatusArr = courseRegCommonFn.checkClash(clashslot, semesterSubId, registerNumber, "ADD", "").split("/");
							regStatusArr = courseRegCommonFn.checkClash(patternId, clashslot, semesterSubId, registerNumber, "ADD", "").split("/");
							regStatusFlag = Integer.parseInt(regStatusArr[0]);
							message = regStatusArr[1];
																	
							if (regStatusFlag == 1) 
							{
								seatWaitFlg = 1;
								for (String courseList : clArr) 
								{		
									switch(courseList)
									{
										case "ELA":
											seatWaitClassNbr = classId2;
											message =  "Lab Component Seats not available";
											break;
										case "EPJ":
											seatWaitClassNbr = classId3;
											message = "Project Component Seats not available";
											break;
										default:
											seatWaitClassNbr = classId1;									
											switch(courseList)
											{
												case "ETH":
												case "TH":
													message = "Theory Component Seats not available";
													break;
												case "SS":
													message = "Softskills Component Seats not available";
													break;
												case "LO":
													message = "Lab Component Seats not available";
													break;
											}
											break;
									}
								
									if (courseAllocationService.getAvailableWaitingSeats(seatWaitClassNbr) <= 0) 
									{
										seatWaitFlg = 2;
										break;
									}
									else
									{
										message = null;
									}
								}
							}						
							
							if (seatWaitFlg == 1)
							{
								wlCount = courseRegistrationWaitingService.getRegisterNumberCRWCount(semesterSubId, registerNumber);
								
								if (wlCount < 2)
								{
									wlCountFlg = 1;
								}
								else
								{
									message = "Only 2 Waiting Courses can be registered.";
								}
							}
							
							String pCompTypeArr = "", pClassIdArr = "", pRegStatus = "", pSubCrTypeArr = "";
							
							if ((regStatusFlag == 1) && (seatWaitFlg == 1) && (wlCountFlg == 1))
							{	
								regTypeCount = 0;
								for (String courseList : clArr) 
								{								
									switch(courseList)
									{
										case "ELA":
											classId = classId2;
											break;
										case "EPJ":
											classId = classId3;
											break;
										default:
											classId = classId1;
											break;
									}
									
									if (pCompTypeArr.equals(""))
									{
										pClassIdArr = classId;
										pCompTypeArr = courseList;
									}
									else
									{
										pClassIdArr = pClassIdArr +"|"+ classId;
										pCompTypeArr = pCompTypeArr +"|"+ courseList;
									}
									regTypeCount = regTypeCount + 1;															
								}
								
								if (regTypeCount != courseTypeArr.size())
								{
									regCompType = 1;
								}
								
								if ((!subCourseOption.equals("")) && (!subCourseOption.equals(null)))
								{
									switch(courseOption)
									{
										case "RR":
										case "RRCE":
										case "GI":
										case "GICE":
										case "RGCE":
										case "RPCE":
										case "RWCE":
											for (String e: subCourseType.split(","))
											{
												if (pSubCrTypeArr.equals(""))
												{
													pSubCrTypeArr = e;
												}
												else
												{
													pSubCrTypeArr = pSubCrTypeArr +"|"+ e;
												}
											}
											break;
										case "CS":
											String[] subCrsOptArr = subCourseOption.split("/");
											subCourseOption = subCrsOptArr[0];
											subCourseType = subCrsOptArr[1];
											subCourseDate = subCrsOptArr[2];
											for (@SuppressWarnings("unused") String courseList : clArr) 
											{
												if (pSubCrTypeArr.equals(""))
												{
													pSubCrTypeArr = subCourseType;
												}
												else
												{
													pSubCrTypeArr = pSubCrTypeArr +"|"+ subCourseType;
												}
											}
											break;
										case "MIN":
										case "HON":
											pSubCrTypeArr = pCompTypeArr;
											break;
										default:
											subCourseType = "";
											subCourseDate = "";
											break;
									}
								}
								
								pRegStatus = courseRegistrationService.courseRegistrationAdd2(semesterSubId, pClassIdArr, 
												registerNumber, courseId, pCompTypeArr, courseOption, regStatus, regCompType, 
												registerNumber, IpAddress, "WL", subCourseOption, "INSERT", pSubCrTypeArr, subCourseDate);
								if (pRegStatus.equals("SUCCESS"))
								{
									msg = "Selected course successfully registered under waiting list";
								}
								else if ((pRegStatus.equals("FAIL")) || (pRegStatus.substring(0, 5).equals("error")))
								{
									message = "Technical error.";
									registrationLogService.addErrorLog(pRegStatus.toString()+"<-CODE->"+courseId, RegErrorMethod+"CourseRegistrationFormController", 
											"processWaitingCoursePROC_INSERT", registerNumber, IpAddress);
									registrationLogService.UpdateLogoutTimeStamp2(IpAddress,registerNumber);
								}
								else
								{
									message = pRegStatus;
								}
															
							}						

						if(WaitingListStatus==1)
							{
								courseRegWaitingList = courseRegistrationWaitingService.getWaitingCourseByClassGroupId(
																	semesterSubId, registerNumber, classGroupId);
							}
							model.addAttribute("info", msg);
							model.addAttribute("courseRegWaitingList", courseRegWaitingList);							
							callCourseRegistrationTypes(registrationOption, pageSize, page, searchType, searchVal, session, model);	
							model.addAttribute("WaitingListStatus", WaitingListStatus);
							urlPage = "mainpages/CourseList::section";
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
		catch(Exception ex)
		{
			model.addAttribute("flag", 1);
			registrationLogService.addErrorLog(ex.toString(), RegErrorMethod+"CourseRegistrationFormController", 
					"processWaitingCourse", registerNumber, IpAddress);
			registrationLogService.UpdateLogoutTimeStamp2(IpAddress,registerNumber);
			urlPage = "redirectpage";
			return urlPage;
		}		
		
		model.addAttribute("infoMessage", message);
		return urlPage;
	}
	
	@PostMapping("processViewSlots")//2
	public String ProcessViewSlots(String courseId,@RequestParam(value = "page", required = false) Integer page,
						@RequestParam(value = "searchType", required = false) Integer searchType,
						@RequestParam(value = "searchVal", required = false) String searchVal, 
						Model model, HttpSession session, HttpServletRequest request)
	{
		String registerNumber = (String) session.getAttribute("RegisterNumber");
		String IpAddress=(String) session.getAttribute("IpAddress");
		Integer WaitingListStatus=(Integer) session.getAttribute("waitingListStatus");
		@SuppressWarnings("unchecked")
		List<String> compCourseList = (List<String>) session.getAttribute("compulsoryCourseList");
		
		Integer updateStatus = 1;
		int allowStatus = 0;	
		String urlPage = "", msg = "", infoMsg = "";
		int pageAuthStatus = 2;
		String pageAuthKey = "";
		pageAuthKey = (String) session.getAttribute("pageAuthKey");
		pageAuthStatus = courseRegCommonFn.validatePageAuthKey(pageAuthKey, registerNumber, 2);
		
		try
		{	
			if ((registerNumber!=null) &&(pageAuthStatus==1))
			{	
				String genericCourseType = "";
				CourseCatalogModel ccm = new CourseCatalogModel();				
				List<CourseAllocationModel> courseAllocationList = new ArrayList<CourseAllocationModel>();
				List<Object[]> courseCostCentre = new ArrayList<Object[]>();
				//List<String> regClassList = new ArrayList<String>();
				//List<String> regSlotList = new ArrayList<String>();
												
				String semesterSubId = (String) session.getAttribute("SemesterSubId");
				String[] classGroupId = session.getAttribute("classGroupId").toString().split("/");
				String ProgramGroupCode = (String) session.getAttribute("ProgramGroupCode");
				String ProgramSpecCode = (String) session.getAttribute("ProgramSpecCode");
				String costCentreCode = (String) session.getAttribute("costCentreCode");
				Date startDate = (Date) session.getAttribute("startDate");
				Date endDate = (Date) session.getAttribute("endDate");
				String startTime = (String) session.getAttribute("startTime");
				String endTime = (String) session.getAttribute("endTime");
				
				String returnVal = courseRegCommonFn.AddorDropDateTimeCheck(startDate, endDate, startTime, endTime, 
										registerNumber, updateStatus, IpAddress);
				String[] statusMsg = returnVal.split("/");
				allowStatus = Integer.parseInt(statusMsg[0]);
				infoMsg = statusMsg[1];
				
								
				switch (allowStatus)
				{
					case 1:
						ccm = courseCatalogService.getOne(courseId);
						if (ccm != null)
						{
							genericCourseType = ccm.getGenericCourseType();
						}
						
						courseAllocationList = courseAllocationService.getCourseAllocationCourseIdList2(semesterSubId, 
													classGroupId, classType, courseId, 
													semesterMasterService.getCourseTypeComponentByGenericType(genericCourseType), 
													ProgramGroupCode, ProgramSpecCode, costCentreCode);
						
						courseCostCentre = courseCatalogService.getCourseCostCentre(CAMPUSCODE);
						
						/*for (CourseRegistrationModel e : courseRegistrationService.getByRegisterNumberCourseId(
																semesterSubId, registerNumber, courseId))
						{
							regClassList.add(e.getClassId());
							regSlotList.add(e.getCourseAllocationModel().getTimeTableModel().getSlotName());
						}*/
																	
																														
						model.addAttribute("CourseDetails", ccm);
						model.addAttribute("page", page);
						model.addAttribute("srhType", searchType);
						model.addAttribute("srhVal", searchVal);
						model.addAttribute("CourseSlotDetails", courseAllocationList);
						model.addAttribute("genericCourseType", genericCourseType);
						model.addAttribute("WaitingListStatus", WaitingListStatus);
						model.addAttribute("tlCompCourseList", compCourseList);
						model.addAttribute("CostCentreLs", courseCostCentre);
						
						//model.addAttribute("tlClashMapList", courseRegCommonFn.getClashSlotStatus(semesterSubId, registerNumber, courseAllocationList));
						//model.addAttribute("tlRegClassList", regClassList);
						//model.addAttribute("tlRegSlotList", regSlotList);
						
						callSlotInformation(model, semesterSubId, registerNumber, courseAllocationList);
												
						urlPage = "mainpages/ViewSlots::section";
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
		catch(Exception ex)
		{
			model.addAttribute("flag", 1);
			registrationLogService.addErrorLog(ex.toString(), RegErrorMethod+"CourseRegistrationFormController", 
					"processViewSlots", registerNumber, IpAddress);
			registrationLogService.UpdateLogoutTimeStamp2(IpAddress,registerNumber);
			urlPage = "redirectpage";
			return urlPage;
		}
		return urlPage;
	}
	
	public int getCompulsoryCourseList(String registrationOption, Integer pageSize, Integer page, Integer searchType,
						String searchVal, String subCourseOption, HttpSession session, Model model, List<String> courseCode)
	{
		String registerNumber = (String) session.getAttribute("RegisterNumber");		
		Pager pager = null;
		int evalPageSize = INITIAL_PAGE_SIZE;
		int evalPage = INITIAL_PAGE;
		evalPageSize = pageSize == null ? INITIAL_PAGE_SIZE : pageSize;
		evalPage = (page == null || page < 1) ? INITIAL_PAGE : page - 1;
		int pageSerialNo = evalPageSize * evalPage;
		int srhType = (searchType == null) ? 0 : searchType;
		String srhVal = (searchVal == null) ? "NONE" : searchVal;
		int pageAuthStatus = 2;
		String pageAuthKey = "";
		pageAuthKey = (String) session.getAttribute("pageAuthKey");
		pageAuthStatus = courseRegCommonFn.validatePageAuthKey(pageAuthKey, registerNumber, 2);
		
		try
		{
			if ((registerNumber!=null) && (pageAuthStatus == 1))
			{
				String semesterSubId = (String) session.getAttribute("SemesterSubId");
				Integer ProgramGroupId = (Integer) session.getAttribute("ProgramGroupId");
				String[] courseSystem = (String[]) session.getAttribute("StudySystem");
				String[] classGroupId = session.getAttribute("classGroupId").toString().split("/");
				String ProgramGroupCode = (String) session.getAttribute("ProgramGroupCode");
				String ProgramSpecCode = (String) session.getAttribute("ProgramSpecCode");
				String costCentreCode = (String) session.getAttribute("costCentreCode");
				
				@SuppressWarnings("unchecked")
				List<Integer> egbGroupId = (List<Integer>) session.getAttribute("EligibleProgramLs");
				Page<CourseCatalogModel> courseCatalogModelPageList = null;
								
				if (registrationOption != null) 
				{
					session.setAttribute("registrationOption", registrationOption);
				} 
				else 
				{
					registrationOption = (String) session.getAttribute("registrationOption");
				}
				
				List<String> courseRegModelList = courseRegistrationService.getRegisteredCourseByClassGroup(
														semesterSubId, registerNumber, classGroupId);
				/*List<String> courseRegWaitingList = courseRegistrationWaitingService.getWaitingCourseByClassGroupId(
														semesterSubId, registerNumber, classGroupId);*/
								
				if (srhType == 0)
				{
					courseCatalogModelPageList = courseCatalogService.getCompulsoryCoursePagination(CAMPUSCODE, courseSystem, 
														egbGroupId, ProgramGroupId.toString(), semesterSubId, classGroupId, 
														classType, courseCode, ProgramGroupCode, ProgramSpecCode, costCentreCode, 
														new PageRequest(evalPage, evalPageSize));
					pager = new Pager(courseCatalogModelPageList.getTotalPages(), courseCatalogModelPageList.getNumber(), 
									BUTTONS_TO_SHOW);
				}
				
				if (courseCatalogModelPageList != null)
				{
					model.addAttribute("courseCatalogModelPageList", courseCatalogModelPageList);
				}
				
				model.addAttribute("registrationOption", registrationOption);
				model.addAttribute("courseRegModelList", courseRegModelList);			
				/*model.addAttribute("courseRegWaitingList", courseRegWaitingList);*/
				model.addAttribute("pageSlno", pageSerialNo);
				model.addAttribute("selectedPageSize", evalPageSize);
				model.addAttribute("pageSizes", PAGE_SIZES);
				model.addAttribute("srhType", srhType);
				model.addAttribute("srhVal", srhVal);
				model.addAttribute("pager", pager);
				model.addAttribute("page", page);
			}
			
		}
		catch(Exception ex)
		{
			logger.catching(ex);
		}
		return 1;
	}
	
	@PostMapping(value="processPageNumbers")
	public String processPageNumbers(Model model, HttpSession session, HttpServletRequest request, 
						@RequestParam(value="pageSize", required=false) Integer pageSize,
						@RequestParam(value="page", required=false) Integer page, 
						@RequestParam(value="searchType", required=false) Integer searchType, 
						@RequestParam(value="searchVal", required=false) String searchVal, 
						@RequestParam(value="totalPage", required=false) Integer totalPage, 
						@RequestParam(value="processType", required=false) Integer processType)
	{
		String registerNumber = (String) session.getAttribute("RegisterNumber");
		String IpAddress = (String) session.getAttribute("IpAddress");
		String urlPage = "";
		
		//System.out.println("registerNumber: "+ registerNumber +" | IpAddress: "+ IpAddress);
		//System.out.println("pageSize: "+ pageSize +" | page: "+ page +" | searchType: "+ searchType 
		//		+" | searchVal: "+ searchVal +" | totalPage: "+ totalPage +" | processType: "+ processType);
		try
		{
			if (registerNumber != null)
			{				
				Pager pager = null;		
				int evalPageSize = INITIAL_PAGE_SIZE;
				int evalPage = INITIAL_PAGE;
				evalPageSize = pageSize == null ? INITIAL_PAGE_SIZE : pageSize;
				evalPage = (page == null || page < 1) ? INITIAL_PAGE : page - 1;
				int pageSerialNo = evalPageSize * evalPage;
				int srhType = (searchType == null) ? 0 : searchType;
				String srhVal = (searchVal == null) ? "NONE" : searchVal;
				
				int pageNumber = evalPage;
				
				if (pageNumber <= 0)
				{
					pageNumber = 0;
				}
				else if ((int)pageNumber >= (int)totalPage)
				{
					pageNumber = totalPage - 1;
				}
				
				pager = new Pager(totalPage, pageNumber, BUTTONS_TO_SHOW);
				
				model.addAttribute("tlTotalPage", totalPage);
				model.addAttribute("tlPageNumber", pageNumber);
				model.addAttribute("pageSlno", pageSerialNo);
				model.addAttribute("selectedPageSize", evalPageSize);
				model.addAttribute("pageSizes", PAGE_SIZES);
				model.addAttribute("srhType", srhType);
				model.addAttribute("srhVal", srhVal);
				model.addAttribute("pager", pager);
				model.addAttribute("page", page);
				
				//System.out.println("totalPage: "+ totalPage +" | pageNumber: "+ pageNumber);
				//System.out.println("pageSerialNo: "+ pageSerialNo +" | evalPageSize: "+ evalPageSize 
				//		+" | srhType: "+ srhType +" | srhVal: "+ srhVal +" | pager: "+ pager 
				//		+" | page: "+ page);
				
				if (processType == 1)
				{
					urlPage = "mainpages/CourseList :: pageNoFrag";
				}
				else if (processType == 2)
				{
					urlPage = "mainpages/CourseList :: pageNoFrag2";
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
			registrationLogService.addErrorLog(e.toString(), RegErrorMethod+"CourseRegistrationFormController", 
					"processPageNumbers", registerNumber, IpAddress);
			registrationLogService.UpdateLogoutTimeStamp2(IpAddress,registerNumber);
			model.addAttribute("flag", 1);
			urlPage = "redirectpage";
			return urlPage;			
		}		

		return urlPage;
	}
	
	//Calling Slot Information When Required.
	public void callSlotInformation(Model model, String semesterSubId, String registerNumber, List<CourseAllocationModel> courseAllocationList)
	{
		List<Object[]> registeredObjectList = new ArrayList<Object[]>();
		Map<String, List<SlotTimeMasterModel>> slotTimeMapList = new HashMap<String, List<SlotTimeMasterModel>>();
		
		registeredObjectList = courseRegistrationService.getRegistrationAndWaitingSlotDetail(semesterSubId, registerNumber);
		slotTimeMapList = semesterMasterService.getSlotTimeMasterCommonTimeSlotBySemesterSubIdAsMap(Arrays.asList(semesterSubId));
		
		model.addAttribute("tlInfoMapList", courseRegCommonFn.getSlotInfo(registeredObjectList, courseAllocationList, slotTimeMapList));
	}
}
