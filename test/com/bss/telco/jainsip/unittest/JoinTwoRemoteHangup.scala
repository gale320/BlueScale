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

import com.bss.telco.api._
import com.bss.telco.jainsip._
import com.bss.telco._

import org.junit._
import Assert._
import java.util.concurrent.CountDownLatch


trait JoinTwoRemoteHangup {
	
	var latch:CountDownLatch = null
 
	def getLatch = latch
	
 	def getTelcoServer() : TelcoServer

 	def getB2BServer() : B2BServer
   	 
 	val alice = getTelcoServer().createConnection("4445556666", "9495557777")
 	val bob = getTelcoServer().createConnection("1112223333", "7147773333")

 
	def runConn() {
 		latch = new CountDownLatch(1)
	          
 		alice.connect(()=>{ 
		  	assertEquals(alice.connectionState, CONNECTED())
		  	bob.connect(()=>{
		  		assertEquals(bob.connectionState, CONNECTED())
				alice.join(bob, ()=>{
				    assertTrue(getTelcoServer.areTwoConnected(alice.asInstanceOf[SipConnection], bob.asInstanceOf[SipConnection]))
				    println("are both connected = ? " + getTelcoServer().areTwoConnected(alice.asInstanceOf[SipConnection], bob.asInstanceOf[SipConnection]))
			        //Now initiate a remote hangup.
			        getB2BServer().findConnByDest("4445556666").foreach( _.disconnect( ()=> {
                        Thread.sleep(50)
                        println("Is bob disconnected now that alice hungup on him? bob = " + bob.connectionState)
                        assertEquals(bob.connectionState,UNCONNECTED())
                        latch.countDown()
			        }))
				})
			})
		})
	}
 }

