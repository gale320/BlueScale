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


import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.bluescale.telco._

import org.bluescale.telco.jainsip._
import org.bluescale.telco.api._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class IncomingRemoteHangup extends FunTestHelper {

    val alice = telcoServer.createConnection("9494443456", "9494443456")

    val joinedLatch = new CountDownLatch(1)

    val disconnectLatch = new CountDownLatch(1)

    var incomingCall:SipConnection = null

    test("Incoming call, answered then a remote hangup") {
        //add ignoring phone number to b2bServer
        val testCall = b2bServer.createConnection("7147579999", "5554443333")
        telcoServer.setIncomingCallback(answerCall)
        testCall.connect().foreach(testCall => println("connected") ) 
        val result = disconnectLatch.await(5,TimeUnit.SECONDS)
		assert(result)
		assert(!telcoServer.areTwoConnected(incomingCall, alice) )
    }

    def answerCall(call:SipConnection) : Unit ={
        //try the call that they're trying to go for.
        alice.disconnectCallback = Some( (c:SipConnection)=> disconnectLatch.countDown())
        incomingCall = call
        for(alice <- alice.connect();  
            _ = println("alice connected!");
            incomingCall <- incomingCall.accept(); 
            incomingCall <- incomingCall.join(alice)) {
                        Thread.sleep(1000)
                        b2bServer.findConnByDest("9494443456").foreach( _.disconnect().foreach( _ =>println( "remote call disconnected!")))
            }
    }
}


