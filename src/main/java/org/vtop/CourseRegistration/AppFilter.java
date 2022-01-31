
package org.vtop.CourseRegistration;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


@WebFilter("/*")
public class AppFilter implements Filter  
{
	//private static final Logger logger = LogManager.getLogger(AppFilter.class);
	
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws ServletException, IOException 
    {
    	HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        HttpSession session=request.getSession();
        String uri=request.getRequestURI();
        String userSessionId=(String)session.getAttribute("userSessionId");
        boolean verifyMenu=  Boolean.parseBoolean(request.getParameter("verifyMenu"));
        String mandatoryScreen=Optional.ofNullable((String) session.getAttribute("MandatoryScreen")).orElse("OFF");
        
        if(isDirectRouting(uri)) 
        {        	
        	chain.doFilter(request, response);        	
        }
        else if(isFirstCall(uri)) 
        {	
        	Integer pass=1;   	
            if(AppGlobalValues.ServerCapacityFixed!=2000) 
            {     // -1 is unlimited login sessions                
            	if(userSessionId==null) 
            	{					// checks first time login            		
	    			if(AppSessionListener.getTotalActiveSession()>AppGlobalValues.ServerCapacityFixed) 
	    			{
	    	    		RequestDispatcher rd=request.getRequestDispatcher("/ServerLimit");
	    	    		rd.forward(request, response);
	    	    		pass=0;
	    			}
            	}
            }
            
        	if((uri.endsWith("adddrop/")) && pass.equals(1))
        	{        		
        		 if(isSessionCarryingOver(userSessionId,session.getId()))
        		 {
        			 RequestDispatcher rd=request.getRequestDispatcher("/AlreadyLogin");
        			 rd.forward(request, response);
        			 pass=0;	            		
           		 }
        	}        	

        	if(pass.equals(1)) 
        	{
        		chain.doFilter(request, response);
        	}        	
        }
        else if(!session.isNew()) 
        {        	
        	if(isSessionCarryingOver(userSessionId,session.getId())) 
        	{
        		boolean isIn = false;
        		
        		if(verifyMenu) 
        		{
        			if(mandatoryScreen.toUpperCase().equals("ON")) 
        			{
        				RequestDispatcher rd=request.getRequestDispatcher("/MandatoryScreenError");
                    	rd.forward(request, response);
    				}
        			else 
        			{
	            		@SuppressWarnings("unchecked")
						List<String> menuCommands=  (List<String>) session.getAttribute("menuCommands");
	            		isIn=isInMenu(Optional.ofNullable(menuCommands),uri);
	
	            		if(!isIn) 
	            		{
	            			RequestDispatcher rd=request.getRequestDispatcher("/MenuPrevillegeError");
	                		rd.forward(request, response);
	            		}
	            		else 
	            		{
	            			chain.doFilter(request, response);
	            		}
    				}
    			}
        		else 
        		{
    				chain.doFilter(request, response);
    			}        		
        	}
        	else 
        	{
        		RequestDispatcher rd=request.getRequestDispatcher("/SessionTimedOut");
        		rd.forward(request, response);
        	}        	
        }
        else if(session.isNew()) 
        {
    		RequestDispatcher rd=request.getRequestDispatcher("/SessionTimedOut");
    		rd.forward(request, response);
        }      
    }

	@Override
	public void init(FilterConfig filterConfig) throws ServletException
	{

	}

	@Override
	public void destroy() 
	{
			
	}
	
	private boolean isInMenu(Optional<List<String>> menus,String uri) 
	{
		boolean isIn=false;
		//String check = "";
		
		if(menus.isPresent()) 
		{
			String isOk=menus.get().stream().filter(value->value!=null).map(value->"/vtop/"+value).filter(value->value.equals(uri)).findFirst().orElse(null);
			isIn=(isOk==null)?false:true;
		}
		
		return isIn;
	}
	
	private boolean isDirectRouting(String uri) 
	{
		  return (uri.contains("ServerLimit") || uri.contains("/MandatoryScreenError") || uri.contains("leave/sms/reply") || uri.contains("gate/in/out") || uri.contains("gate/image") || uri.contains("SessionTimedOut") ||  uri.contains("processLogout") || uri.contains("AlreadyLogin") || uri.contains("MenuPrevillegeError") || uri.contains("assets")); 		
	}
	
	private boolean isFirstCall(String uri) 
	{
		return (uri.equals("/") || uri.contains("adddrop") || uri.contains("processStudentLogin"));
	}
	
	private boolean isSessionCarryingOver(String userSessionId,String currentId) 
	{
		return (userSessionId!=null && userSessionId.equals(currentId));
	}
}
