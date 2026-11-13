package com.narcic.doh.net

import android.util.Log
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class VpnAdapter () {
    private var pythonJob: Job? = null
    private var smlwb: PyObject? = null

    fun start(port: Int, lan: Boolean){
        stop()
        try {
            pythonJob = CoroutineScope(Dispatchers.IO).launch {
                try {
                    val module = Python.getInstance().getModule("smlwb")
                    smlwb = module
                    module.callAttr("main", port, lan)
                } catch (e: Exception) {
                    Log.e("Sml", "proxy stopped", e)
                    smlwb = null
                }
            }
        } catch (e: Exception) {
            stop()
        }
    }
    fun stop(){
        try {
            // closes the listening socket so the port can be reused / changed
            smlwb?.callAttr("stop")
        } catch (e: Exception) {
            Log.e("Sml", "stop()", e)
        }
        pythonJob?.cancel("stop")
        smlwb = null
        pythonJob = null
    }

    fun isOn():Boolean{
        return (pythonJob != null) && (smlwb != null)
    }
}
