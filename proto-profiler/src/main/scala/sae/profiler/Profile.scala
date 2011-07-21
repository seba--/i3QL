package sae.profiler

import sae.util.Timer

/**
 * 
 * Author: Ralf Mitschke
 * Created: 20.07.11 15:05
 *
 */
case class Profile(codeBase: String, profileName: String, timers: Array[Timer], sampleSize: Int)
{
    def measurements = timers.size;
}