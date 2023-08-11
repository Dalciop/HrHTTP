package pl.dalcop.tentnohttp

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.TextView
import fi.iki.elonen.NanoHTTPD

class HttpServer {
    companion object {
        private lateinit var httpServer: NanoHTTPD

        fun isRunning(): Boolean {
            return try {
                httpServer.isAlive()
            } catch(e: Exception) {
                false
            }
        }

        fun serveHTTP(context: Context, intent: Intent, serverInfoTextView: TextView, heartRateTextView: TextView): Boolean {
            httpServer = object : NanoHTTPD("0.0.0.0", Network.port) {
                override fun serve(session: IHTTPSession): Response {
                    if ("/hr" == session.uri) {
                        val heartRateValue = HeartBeat.getHeartBeat(intent, context, heartRateTextView)
                        return newFixedLengthResponse("$heartRateValue")
                    }
                    return newFixedLengthResponse("Invalid endpoint")
                }
            }

            return try {
                httpServer.start()
                true
            } catch (e: Exception) {
                serverInfoTextView.text = "Error starting HTTP server"
                Log.e("HTTPServer", "Error starting HTTP server", e)
                false
            }
        }

        fun stopHTTP() {
            httpServer.stop()
        }
    }
}