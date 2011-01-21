/*
*  
* This file is part of BlueScale.
*
* BlueScale is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* BlueScale is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
* 
* You should have received a copy of the GNU Affero General Public License
* along with BlueScale.  If not, see <http://www.gnu.org/licenses/>.
* 
* Copyright Vincent Marquez 2010
* 
* 
* Please contact us at www.BlueScaleSoftware.com
*
*/

package com.bss.telco.jainsip.unittest
 

import org.junit._
import Assert._
import com.bss.telco.jainsip.test._
import java.util._
import com.bss.telco.jainsip._

class LeakCallHangup  extends junit.framework.TestCase with SimpleCall  {
	
	val telcoServer  = new SipTelcoServer( "127.0.0.1", 4000, "127.0.0.1", 4001) 
	val b2bServer = new B2BServer( "127.0.0.1", 4001, "127.0.0.1", 4000)
 
	def getCounter = None
	 
	b2bServer.start()
	telcoServer.start()
 
	@Override 
	def getTelcoServer() = telcoServer;

	@Test
	def testNoLeaks() = {
	  	System.gc()
	  	//just get things initailized, etc.
	   for (i <- 1 to 10) {
	  		 runConn()
	  		 getLatch.await()
	  		 
	  	}
	  	val freeBeforeMem = Runtime.getRuntime().freeMemory()
	  	 
	  	for (i <- 1 to 1000) {
	  		 runConn()
	  		 getLatch.await()
	   	}
	   
	  System.gc()
	  val freeAfterMem = Runtime.getRuntime().freeMemory() 
	  println(" freeBefore = " + freeBeforeMem/1024 + " | freeAfterMem = " + freeAfterMem/1024)
	  assertTrue(freeBeforeMem < freeAfterMem )
	   
 	 
	}
  
 
}
 
