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
package com.bss.blueml

import scala.xml._
import com.bss.telco.api._
import com.bss.util.Util

object BlueMLParser {

    def parse(str:String) =
       for (verb <- (XML.loadString(str) \\ "Response") \ "_" )
            yield parseVerb(verb)
    
    private def parseVerb(n:Node) : BlueMLVerb = {
        println("parseVerb node = " + n)
        n.label match {
            case "Dial" => parseDial(n)
            case "Say" => throw new UnsupportedOperationException("Say")
            case "Play" => parsePlay(n)
            case "Gather"=>throw new UnsupportedOperationException("Gather")
            case "Record"=>throw new UnsupportedOperationException("Record")
            case _ => null
        }
    }

    private def parseDial(n:Node) : Dial = 
        new Dial( Util.GetNonEmpty((n \ "Number").text, n.text),
                  (n \ "CallerId").text,
                  (n \"Action").text )
                  
   private def parsePlay(n:Node) : Play = {
        return null
    }

}


trait BlueMLVerb


class Play(val loop:Int,
           val url:String) extends BlueMLVerb {
   

}

class Dial(val number:String,
           val callerId:String,
           val url:String) extends BlueMLVerb {

}

 



