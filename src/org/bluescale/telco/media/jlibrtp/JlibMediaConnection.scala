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

package org.bluescale.telco.media.jlibrtp

import javax.sdp._

import org.bluescale.telco.SdpHelper;

import org.bluescale.telco.api._
import org.bluescale.telco._
import java.net.DatagramSocket
import jlibrtp.RTPSession
import jlibrtp.RTPAppIntf
import jlibrtp.DataFrame
import jlibrtp.Participant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class JlibMediaConnection(telco:TelcoServer) extends MediaConnection {

	println("Made JlibMediaConncetion, my hashcode = " + this.hashCode() + "!")
    private val rtpPort = JlibMediaConnection.getRtpSockets()
    
    private val rtpSession = new RTPSession(rtpPort._1, rtpPort._2)
    
    val listeningSdp = SdpHelper.createSdp(rtpPort._1.getLocalPort(), telco.contactIp)
    
    rtpSession.naivePktReception(true)
    rtpSession.RTPSessionRegister( new RTPAppIntf {
    		override def receiveData(frame:DataFrame, participant:Participant) =
    		  receive(frame, participant)
    			  
    		override def userEvent(etype:Int, participants:Array[Participant]) =
    		 	Unit
    			    
    		override def frameSize(payloadType:Int) = 1
    	},null, null);
    
    
    private var _joinedTo:Option[Joinable[_]] = None
    
    private var _recordedFiles = List[String]()
    private var _playedFiles   = List[String]()
    
    override def playedFiles = _playedFiles 
    
	override def recordedFiles = _recordedFiles

	private var connState = UNCONNECTED()
    
    override def joinedTo = _joinedTo
    
    override def join(conn:Joinable[_], f:()=>Unit) =
      	conn.connect(this, false, ()=> {
    		this._joinedTo = Some(conn)
    		f()
    	})
	
    
    override def sdp = 
      listeningSdp
    
    def connectionState = connState

    def joinPlay(url:String, conn:Joinable[_], f:()=>Unit) = join(conn, ()=> {
    	play(url, f)
    })
    
    
    def receive(frame:DataFrame, participant:Participant)  =
      MediaFileManager.addMedia(this, frame.getConcatenatedData())
    
    
    override def play(url:String, f:()=>Unit) =
    	joinedTo.foreach( joined => {
    		//fixme, do we need listening ports to be in the RTPSession?
    		rtpSession.addParticipant(new Participant("",
    				SdpHelper.getMediaPort(joined.sdp), 		//RTP
    				SdpHelper.getMediaPort(joined.sdp)+1)) 	//RTCP
    			val inputStream = MediaFileManager.getInputStream(url)
    			//TODO: send the packets
    			val bytes = new Array[Byte](1024)
    			while (inputStream.read(bytes) != -1)
    				rtpSession.sendData(bytes)
    			
    			rtpSession.endSession()
    			_playedFiles = url :: _playedFiles
    			f()
    		
    	})
    
    override def cancel(f:()=>Unit) {
      
    }

    //PROTECTED STUFF FOR JOINABLE
    override protected[telco] def connect(join:Joinable[_], connectedCallback:()=>Unit) {
    	//store SDP somewhere
    	connectedCallback()
    }

    override protected[telco] def connect(join:Joinable[_], connectAnyMedia:Boolean, connectedCallback:()=>Unit) {//doesn't need to be here? 
    	connectedCallback()
	}
    
    protected[telco] def onConnect(f:()=>Unit) = f() //more to do? 

    protected[telco] def unjoin(f:()=>Unit) = {
    	finishListen()
    	println(" unjoin, mc = " + this.hashCode() + " files count = " + _recordedFiles.size)
    	f()
    	unjoinCallback.foreach(_(joinedTo.get,this))
    }
    
    private def finishListen() =
    	MediaFileManager.finishAddMedia(this).foreach(newFile => _recordedFiles = newFile :: _recordedFiles)
    
}

object JlibMediaConnection {
	val atomicInt = new AtomicInteger(1234)	
  
	//phone nunew mber
	def mediaConnections = new ConcurrentHashMap[String, JlibMediaConnection]()
	
	def getRtpSockets() : (DatagramSocket,DatagramSocket) = {
      val i = atomicInt.getAndAdd(2)
      return (new DatagramSocket(i), new DatagramSocket(i+1))
    }
}





	
