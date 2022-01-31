package org.vtop.CourseRegistration.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vtop.CourseRegistration.model.CourseRegistrationWaitingModel;
import org.vtop.CourseRegistration.repository.CourseRegistrationWaitingRepository;


@Service
@Transactional("academicsTransactionManager")
public class CourseRegistrationWaitingService
{		
	@Autowired private CourseRegistrationWaitingRepository courseRegistrationWaitingRepository;
	
	
	public List<CourseRegistrationWaitingModel> getByRegisterNumberAndCourseCode(String semesterSubId, 
													String registerNumber, String courseCode)
	{
		return courseRegistrationWaitingRepository.findByRegisterNumberAndCourseCode(semesterSubId, 
					registerNumber, courseCode);
	}
	
	public List<CourseRegistrationWaitingModel> getByRegisterNumberCourseIdByClassGroupId(String semesterSubId, 
													String registerNumber, String courseId, String[] classGroupId)
	{
		return courseRegistrationWaitingRepository.findByRegisterNumberCourseIdByClassGroupId(semesterSubId, 
					registerNumber, courseId, classGroupId);
	}
	
	public List<Object[]> getWaitingSlots2(String semesterSubId, String registerNumber)
	{
		return courseRegistrationWaitingRepository.findWaitingSlots2(semesterSubId, registerNumber);
	}
	
	public List<String> getWaitingCourseByClassGroupId(String semesterSubId, String registerNumber, 
							String[] classGroupId)
	{
		return courseRegistrationWaitingRepository.findWaitingCourseByClassGroupId(semesterSubId, 
					registerNumber, classGroupId);
	}
	
	//Course Count or Credit
	public Integer getRegisterNumberCRWCount(String semesterSubId, String registerNumber)
	{
		Integer tempCount = 0;
		
		tempCount = courseRegistrationWaitingRepository.findRegisterNumberCRWCount(semesterSubId, registerNumber);
		if (tempCount == null)
		{
			tempCount = 0;
		}
		
		return tempCount;
	}
	
	public Integer getRegisterNumberCRWCountByClassGroupId(String semesterSubId, String registerNumber, 
						String[] classGroupId)
	{
		Integer tempCount = 0;
		
		tempCount = courseRegistrationWaitingRepository.findRegisterNumberCRWCountByClassGroupId(semesterSubId, 
						registerNumber, classGroupId);
		if (tempCount == null)
		{
			tempCount = 0;
		}
		
		return tempCount;
	}
		
	public Integer getRegCreditByRegisterNumberAndNCCourseCode(String semesterSubId, String registerNumber, 
						List<String> ncCourseCode)
	{
		Integer tempCount = 0;
		
		tempCount = courseRegistrationWaitingRepository.findRegCreditByRegisterNumberAndNCCourseCode(semesterSubId, 
						registerNumber, ncCourseCode);
		if (tempCount == null)
		{
			tempCount = 0;
		}
		
		return tempCount;
	}	
	
	public void deleteByRegisterNumberCourseId(String semesterSubId, String registerNumber, String courseId)
	{		
		courseRegistrationWaitingRepository.deleteByRegisterNumberCourseId(semesterSubId, registerNumber, courseId);
	}
	
	public List<Object[]> getWaitingCourseByRegNoWithRank(String semesterSubId, String registerNumber)
	{
		return courseRegistrationWaitingRepository.findWaitingCourseByRegNoWithRank(semesterSubId, registerNumber);
	}
	
	public List<Object[]> getWaitingCourseByRegNoWithRankByClassGroupId(String semesterSubId, String registerNumber, 
								String[] classGroupId)
	{
		return courseRegistrationWaitingRepository.findWaitingCourseByRegNoWithRankByClassGroupId(semesterSubId, 
					registerNumber, classGroupId);
	}
	
	public List<Object[]> getCourseOptionByRegisterNumberAndCourseCode(String semesterSubId, String registerNumber, 
								String courseCode)
	{
		return courseRegistrationWaitingRepository.findCourseOptionByRegisterNumberAndCourseCode(semesterSubId, 
					registerNumber, courseCode);
	}
	
	//UE Registered Course(s)
	public List<String> getUECourseByRegisterNumberAndCourseOption(String semesterSubId, String registerNumber, 
							String courseCategory)
	{
		List<String> tempCourseList = new ArrayList<String>();
		
		if (courseCategory.equals("UC"))
		{
			tempCourseList = courseRegistrationWaitingRepository.findUECourseByRegisterNumberAndCourseOption(semesterSubId, 
									registerNumber, Arrays.asList("RUCUE"));
		}
		else if (courseCategory.equals("PE"))
		{
			tempCourseList = courseRegistrationWaitingRepository.findUECourseByRegisterNumberAndCourseOption(semesterSubId, 
									registerNumber, Arrays.asList("RPEUE","RGA"));
		}
		else if (courseCategory.equals("UE"))
		{
			tempCourseList = courseRegistrationWaitingRepository.findUECourseByRegisterNumberAndCourseOption(semesterSubId, 
									registerNumber, Arrays.asList("RUCUE","RPEUE","RGA"));
		}
		
		return tempCourseList;
	}
	
	
	//Course Registration Waiting Move
	public void addWaitingToWaitingMove(String semesterSubId, String registerNumber, String courseId, 
			int waitStatus, String ipaddress)
	{		
		courseRegistrationWaitingRepository.insertWaitingToWaitingMove(semesterSubId, registerNumber, 
				courseId, waitStatus, ipaddress);
	}
	
	
	/*public CourseRegistrationWaitingModel saveOne(CourseRegistrationWaitingModel courseRegistrationWaitingModel)
	{
		return courseRegistrationWaitingRepository.save(courseRegistrationWaitingModel);
	}
	
	public CourseRegistrationWaitingModel getOne(CourseRegistrationWaitingPKModel courseRegistrationWaitingPKModel)
	{
		return courseRegistrationWaitingRepository.findOne(courseRegistrationWaitingPKModel);
	}
		
	public List<CourseRegistrationWaitingModel> getAll(String semesterSubId)
	{
		return courseRegistrationWaitingRepository.findBySemesterSubId(semesterSubId);
	}
		
	public List<CourseRegistrationWaitingModel> getByRegisterNumber(String semesterSubId, String registerNumber)
	{
		return courseRegistrationWaitingRepository.findByRegisterNumber(semesterSubId, registerNumber);
	}
	
	public List<CourseRegistrationWaitingModel> getByRegisterNumberCourseId(String semesterSubId, 
													String registerNumber, String courseId)
	{
		return courseRegistrationWaitingRepository.findByRegisterNumberCourseId(semesterSubId, 
					registerNumber, courseId);
	}*/
	
	/*public List<String> getWaitingSlots(String semesterSubId, String registerNumber)
	{
		return courseRegistrationWaitingRepository.findWaitingSlots(semesterSubId, registerNumber);
	}*/
	
	/*public List<String> getWaitingCourse(String semesterSubId, String registerNumber)
	{
		return courseRegistrationWaitingRepository.findWaitingCourse(semesterSubId, registerNumber);
	}*/
	
	/*public List<CourseRegistrationWaitingModel> getByClassId(String semesterSubId, String classId)
	{
		return courseRegistrationWaitingRepository.findByClassId(semesterSubId, classId);
	}
	
	public void statusUpdate(String semesterSubId, String registerNumber, String courseId, 
					Integer waitingStatus)
	{		
		courseRegistrationWaitingRepository.updateStatusNoByRegisterNumberCourseId(semesterSubId, 
				registerNumber, courseId, waitingStatus);
	}*/
	
	/*public Integer getRegisterNumberAudCount(String semesterSubId, String registerNumber)
	{
		Integer tempCount = 0;
		
		tempCount = courseRegistrationWaitingRepository.findRegisterNumberAudCount(semesterSubId, registerNumber);
		if (tempCount == null)
		{
			tempCount = 0;
		}
		
		return tempCount;
	}
	
	public Integer getRegisterNumberGICount(String semesterSubId, String registerNumber)
	{
		Integer tempCount = 0;
		
		tempCount = courseRegistrationWaitingRepository.findRegisterNumberGICount(semesterSubId, registerNumber);
		if (tempCount == null)
		{
			tempCount = 0;
		}
		
		return tempCount;
	}*/
	
	/*public Integer getRegisterNumberRCCount(String semesterSubId, String registerNumber)
	{
		Integer tempCount = 0;
		
		tempCount = courseRegistrationWaitingRepository.findRegisterNumberRCCount(semesterSubId, registerNumber);
		if (tempCount == null)
		{
			tempCount = 0;
		}
		
		return tempCount;
	}*/
}
