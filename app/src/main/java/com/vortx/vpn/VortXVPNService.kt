package com.vortx.vpn
import android.app.*
import android.content.Intent
import android.net.VpnService
import android.os.*
import androidx.core.app.NotificationCompat
import java.io.*

class VortXVPNService:VpnService(){
 override fun onStartCommand(i:Intent?,f:Int,s:Int):Int{
  val c="vortx"
  if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel(c,"VortX VPN",NotificationManager.IMPORTANCE_LOW))
  val p=PendingIntent.getActivity(this,0,Intent(this,MainActivity::class.java),PendingIntent.FLAG_IMMUTABLE)
  startForeground(1,NotificationCompat.Builder(this,c).setContentTitle("VortX VPN").setContentText("Подключено").setSmallIcon(android.R.drawable.ic_lock_lock).setContentIntent(p).setOngoing(true).build())
  Builder().setSession("VortX").addAddress("10.0.0.2",32).addRoute("0.0.0.0",0).addDnsServer("8.8.8.8").addDnsServer("1.1.1.1").setMtu(1500).establish()
  return START_STICKY
 }
}
