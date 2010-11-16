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

package com.bss.server

import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.io._
import org.mortbay.jetty.servlet.ServletHandler 
import org.mortbay.jetty.servlet.ServletHandler
import org.mortbay.jetty.handler.ContextHandlerCollection
import org.mortbay.jetty.Server
import org.mortbay.jetty.servlet.Context
import org.mortbay.jetty.servlet.ServletHolder
import org.mortbay.jetty.bio.SocketConnector
import org.mortbay.jetty.handler.AbstractHandler
import com.bss.telco.api._
import com.bss.telco.jainsip.SipTelcoServer
import java.net._
import scala.xml._

class WebServer(apiPort:Int,
                adminPort:Int,
                telcoServer:TelcoServer,
                callbackUrl:String) {
    
    private val wserver = new Server()
    initWebServer()
    println(" telcoServe r= " + telcoServer)
    println(" ballbackUrl = " + callbackUrl)
    telcoServer.setIncomingCallback( (conn:SipConnection)=> BlueML.postBackStatus(callbackUrl, conn) )


    def initWebServer() {
        val apiConnector = new SocketConnector()
        apiConnector.setPort(apiPort)
        wserver.setConnectors( List(apiConnector).toArray )
        val context = new Context(wserver, "/", Context.SESSIONS)
        context.addServlet( new ServletHolder( new CallServlet(telcoServer) ), "/*" )
        wserver.start()
    }

    def stop() =
        wserver.stop()
}


class CallServlet(telcoServer:TelcoServer) extends HttpServlet {
    
    override def doGet(request:HttpServletRequest, response:HttpServletResponse) {
        val to      = request.getParameter("To")
        val from    = request.getParameter("From") 
        val url     = request.getParameter("Url")

        val conn = telcoServer.createConnection(to, from)
        conn.connect(() => {
            BlueML.postBackStatus(url, conn)
            //send status to the url
        })

        //print out XML to the page!

        val response = BlueML.getCallResponse(conn.connectionid, to, from, "progressing")
    }

   
}

object BlueML {
    def postBackStatus(url:String, conn:SipConnection) = 
        Option(BlueML.postToUrl(url, getConnectionMap(conn) ) ) match {
            case Some(xml) =>   println(xml)
                                handleBlueML(xml, conn)
            case None => //nothing to do here
        }
         
   def getConnectionMap(conn:SipConnection) =   Map( "CallId"->conn.connectionid,
                                                  "From"-> conn.origin,
                                                  "To" -> conn.destination,
                                                  "CallStatus" -> conn.connectionState.toString(),
                                                  "Direction" -> conn.direction.toString() )    
    
    def handleBlueML(str:String, conn:SipConnection) {
        val elem = XML.loadString(str) \ "BlueML"
        
        Some(elem).foreach( _.text match {
                    case "Dial" => conn.accept( ()=> println("ok connect to the other call, then join here") )
                    case _ =>
                } )
        

    }
    

    def readAll(reader:BufferedReader) : String = 
        Option( reader.readLine() ) match {
            case None   => ""
            case Some(x)=> readAll(reader).concat(x)
        }                   
   

    def postToUrl(url:String, params:Map[String, String]) : String = {
        val data = params
                    .map({ case (key, value) =>  URLEncoder.encode( key, "UTF-8") + "=" +URLEncoder.encode( value, "UTF-8") })
                    .reduceLeft(_ + "&" + _)
        println("data = " + data)           
             
        val urlConn = new URL(url).openConnection()
        urlConn.setDoOutput(true)
        val os = new OutputStreamWriter(urlConn.getOutputStream())
        try {
            os.write(data)
            os.flush()
            val reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream() ))
            val response = readAll(reader)
            println("response = " + response)
            reader.close()
            return response
        } finally {
            os.close()
        }
    }

    def getCallResponse(connId:String, to:String, from:String, status:String ) : String = {
        return  (<BlueXml>
                   <DateCreated></DateCreated>
                    <CallId>{connId}</CallId>
                    <To>{to}</To>
                    <From>{from}</From>
                    <Status>{status}</Status>
                    <Direction></Direction>
                </BlueXml>).toString()
    }
}


