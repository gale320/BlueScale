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
* Please contact us at www.BlueScale.org
*
*/
package org.bluescale.telco.jainsip.unittest

import org.bluescale.telco.api._
import org.bluescale.telco.jainsip._
import org.bluescale.telco._

import org.junit._
import Assert._
import java.util.concurrent.CountDownLatch


trait JoinTwoRemoteHangup {
	
	var latch:CountDownLatch = null
 
	def getLatch = latch
	
 	def getTelcoServer() : TelcoServer

 	def getB2BServer() : B2BServer

 	getTelcoServer().setDisconnectedCallback(disconnected)

 	val alice = getTelcoServer().createConnection("4445556666", "9495557777")
 	val bob = getTelcoServer().createConnection("1112223333", "7147773333")
    
    def disconnected(call:SipConnection) {
        Thread.sleep(1000)
        println(" alice = " + alice)
        println(" bob = " + bob )
        if (alice.connectionState == UNCONNECTED() &&
            bob.connectionState == UNCONNECTED())
            latch.countDown()
    }
 
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
			        Thread.sleep(1000)
			        getB2BServer().findConnByDest("4445556666").foreach( _.disconnect( ()=> println("disconnected") ))
				})
			})
		})
	}
 }


