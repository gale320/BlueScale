/*
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

package com.bss.util

import com.bss.telco.api._
import com.bss.telco.jainsip.SipTelcoServer
import java.net._
import scala.xml._
import java.io._

object WebUtil {

    
    def readAll(reader:BufferedReader) : String = 
        Option( reader.readLine() ) match {
            case None   => ""
            case Some(x)=> x.concat(readAll(reader))
        }                   
   
    
    def postToUrl(url:String, params:Map[String, String]) : String = {
        println("posting to " + url)
        val data = params
                    .map({ case (key, value) =>  
                        URLEncoder.encode( key, "UTF-8") + "=" +URLEncoder.encode( value, "UTF-8") })
                    .reduceLeft(_ + "&" + _)
        
        val urlConn = new URL(url).openConnection()
        urlConn.setDoOutput(true)
        val os = new OutputStreamWriter(urlConn.getOutputStream())
        try {
            os.write(data)
            os.flush()
            val reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream() ))
            val response = readAll(reader)
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

