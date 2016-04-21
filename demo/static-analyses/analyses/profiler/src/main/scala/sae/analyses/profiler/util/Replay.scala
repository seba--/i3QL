/* License (BSD Style License):
 * Copyright (c) 2011
 * Department of Computer Science
 * Technische Universität Darmstadt
 * All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  - Neither the name of the Software Technology Group or Technische 
 *    Universität Darmstadt nor the names of its contributors may be used to 
 *    endorse or promote products derived from this software without specific 
 *    prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 */
package sae.analyses.profiler.util

import java.io.File

/**
 * Central entry point to lyrebird recorder.
 * These class handles to replay previous recorded events by Lyrebird.recorder
 *
 * @author Malte Viering
 */
class Replay(val eventSets: List[Seq[ReplayEvent]])
{

    def this(location: File) {
        this (new ReplayReader (location).getAllEventSets)
    }

    // TODO this code smells: why is there not fChanged?? There is no explanation whatsoever... no examples...
    def processAllEventSets(fAdd: File ⇒ _, fRemove: File ⇒ _) {
        eventSets.foreach (processEventSet (_, fAdd, fRemove))
    }

    // TODO this code smells: why is there not fChanged? There is no explanation whatsoever... no examples...
    def processEventSet(eventSet: Seq[ReplayEvent], fAdd: File ⇒ _, fRemove: File ⇒ _) {
        eventSet.foreach (
			{
                case ReplayEvent (ReplayEventType.ADDED, _, _, file, _) ⇒ fAdd (file)
                case ReplayEvent (ReplayEventType.REMOVED, _, _, file, Some (prev)) ⇒
                    if (prev.eventType != ReplayEventType.REMOVED) {
                        fRemove (prev.eventFile)
                    }
                case ReplayEvent (ReplayEventType.REMOVED, _, _, file, None) ⇒ // do nothing
                case ReplayEvent (ReplayEventType.CHANGED, _, _, file, Some (prev)) ⇒ {
                    if (prev.eventType != ReplayEventType.REMOVED)
                        fRemove (prev.eventFile)
                    fAdd (file)
                }
                case ReplayEvent (ReplayEventType.CHANGED, _, _, file, None) ⇒ {
                    fAdd (file)
                }
			}
		)
    }
}