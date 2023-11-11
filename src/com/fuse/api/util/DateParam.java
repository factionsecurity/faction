package com.fuse.api.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class DateParam {
	  private final Date date;

	  public DateParam(String dateStr) throws WebApplicationException {
	    if (isEmpty(dateStr)) {
	      this.date = null;
	      return;
	    }
	    final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	    try {
	      this.date = dateFormat.parse(dateStr);
	    } catch (ParseException e) {
	      throw new WebApplicationException(Response.status(Status.BAD_REQUEST)
	        .entity("Couldn't parse date string: " + e.getMessage())
	        .build());
	    }
	  }

	  private boolean isEmpty(String dateStr) {
		if(dateStr == null)
			return true;
		else if(dateStr.equals(""))
			return true;
		else
			return false;
	}

	public Date getDate() {
	    return date;
	  }
	}