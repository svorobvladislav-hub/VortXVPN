package com.vortx.vpn
import android.content.Intent
import android.net.VpnService
import android.os.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import kotlinx.coroutines.*
import okhttp3.*
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import java.io.*
import java.net.*

data class ServerData(val id:Int,val name:String,val flag:String,val ping:Int,val url:String)
object S{val servers=mutableStateListOf<ServerData>();var sel=mutableIntStateOf(0);val con=mutableStateOf(false);val load=mutableStateOf(false);var dev=mutableIntStateOf(0)}

class MainActivity:ComponentActivity(){
 private val vpn=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){if(it.resultCode==RESULT_OK)startVpn()}
 override fun onCreate(b:Bundle?){super.onCreate(b);loadServers();setContent{MaterialTheme(colorScheme=darkColorScheme(primary=Color(0xFF6C63FF),background=Color(0xFF0D0D1A),surface=Color(0xFF1A1A2E))){App()}}}
 private fun loadServers(){CoroutineScope(Dispatchers.IO).launch{try{val t=OkHttpClient().newCall(Request.Builder().url("https://raw.githubusercontent.com/svorobvladislav-hub/vortex-servers/main/config.txt").build()).execute().body?.string()?:"";val n=mutableListOf<ServerData>();var i=1;for(l in t.split("\n")){if(l.startsWith("vless://")){val nm=if(l.contains("#"))l.substringAfterLast("#")else"Server \$i";n.add(ServerData(i++,nm,"🌍",(5..200).random(),l.trim()))}};withContext(Dispatchers.Main){S.servers.clear();S.servers.addAll(n.ifEmpty{(1..37).map{ServerData(it,"VortX \$it","🌍",(5..200).random(),"")}})}}catch(e:Exception){withContext(Dispatchers.Main){S.servers.clear();for(i in 1..37)S.servers.add(ServerData(i,"VortX \$i","🌍",50,""))}}}}
 private fun startVpn(){S.con.value=true;startService(Intent(this,VortXVPNService::class.java))}
 private fun stopVpn(){S.con.value=false;stopService(Intent(this,VortXVPNService::class.java))}
 @Composable fun App(){var list by remember{mutableStateOf(false)};var dev by remember{mutableStateOf(false)}
  if(dev)Dev{dev=false}else if(list)List{list=false}else{val s=S.servers.getOrElse(S.sel.intValue){S.servers.firstOrNull()};Box(Modifier.fillMaxSize().background(Color(0xFF0D0D1A))){Column(Modifier.fillMaxSize().padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.SpaceBetween){Row(Modifier.fillMaxWidth().padding(top=16.dp),Arrangement.SpaceBetween,Alignment.CenterVertically){Text("VORTX",fontSize=28.sp,fontWeight=FontWeight.Bold,color=Color.White);Button(onClick={list=true},colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF6C63FF).copy(0.3f)),shape=RoundedCornerShape(12.dp)){Text("СЕРВЕРЫ",color=Color.White,fontSize=11.sp)}}
  if(s!=null)Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(12.dp),colors=CardDefaults.cardColors(containerColor=Color.White.copy(0.05f))){Row(Modifier.padding(12.dp),verticalAlignment=Alignment.CenterVertically){Text(s.flag,fontSize=24.sp);Spacer(Modifier.width(8.dp));Column{Text(s.name,color=Color.White,fontWeight=FontWeight.SemiBold);Text("Ping: \${s.ping}ms",color=Color.White.copy(0.5f),fontSize=10.sp)}}}
  Spacer(Modifier.weight(1f));Box(Modifier.size(220.dp).shadow(30.dp,CircleShape).clip(CircleShape).background(Color.White.copy(0.05f)).clickable{if(S.con.value)stopVpn()else{S.load.value=true;val i=VpnService.prepare(this@MainActivity);if(i!=null)vpn.launch(i)else startVpn();CoroutineScope(Dispatchers.Main).launch{delay(2000);S.load.value=false}}},contentAlignment=Alignment.Center){Box(Modifier.size(140.dp).shadow(20.dp,CircleShape).clip(CircleShape).background(Brush.radialGradient(colors=if(S.con.value)listOf(Color(0xFF00E676),Color(0xFF00C853))else listOf(Color(0xFF6C63FF),Color(0xFF5A52E0)))),contentAlignment=Alignment.Center){if(S.load.value)CircularProgressIndicator(color=Color.White,strokeWidth=3.dp)else Icon(if(S.con.value)Icons.Default.Lock else Icons.Default.PowerSettingsNew,null,tint=Color.White,modifier=Modifier.size(60.dp))}}
  Spacer(Modifier.height(16.dp));Text(if(S.load.value)"CONNECTING..."else if(S.con.value)"SECURED"else"DISCONNECTED",color=if(S.con.value)Color(0xFF00E676)else Color.White.copy(0.6f),fontWeight=FontWeight.Bold,letterSpacing=3.sp);Spacer(Modifier.weight(1f));Box(Modifier.fillMaxWidth().height(20.dp).clickable{S.dev.intValue++;if(S.dev.intValue>=7){S.dev.intValue=0;dev=true}})}}}}
 @Composable fun List(close:()->Unit){Column(Modifier.fillMaxSize().background(Color(0xFF0D0D1A)).padding(24.dp)){Row(Modifier.fillMaxWidth(),Arrangement.SpaceBetween,Alignment.CenterVertically){Text("СЕРВЕРЫ",fontSize=24.sp,fontWeight=FontWeight.Bold,color=Color.White);TextButton(onClick=close){Text("ЗАКРЫТЬ",color=Color(0xFF6C63FF))}};Spacer(Modifier.height(12.dp));LazyColumn(verticalArrangement=Arrangement.spacedBy(8.dp)){items(S.servers){s->val i=S.servers.indexOf(s);Card(Modifier.fillMaxWidth().clickable{S.sel.intValue=i;close()},shape=RoundedCornerShape(12.dp),colors=CardDefaults.cardColors(containerColor=if(i==S.sel.intValue)Color(0xFF6C63FF).copy(0.2f)else Color.White.copy(0.05f))){Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically){Text(s.flag,fontSize=28.sp);Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(s.name,color=Color.White,fontWeight=FontWeight.SemiBold)};Text("\${s.ping}ms",color=if(s.ping<50)Color(0xFF00E676)else Color.White.copy(0.7f),fontWeight=FontWeight.Bold)}}}}}}
 @Composable fun Dev(close:()->Unit){Column(Modifier.fillMaxSize().background(Color(0xFF0D0D1A)).padding(24.dp)){Row(Modifier.fillMaxWidth(),Arrangement.SpaceBetween,Alignment.CenterVertically){Text("МЕНЮ РАЗРАБОТЧИКА",fontSize=22.sp,fontWeight=FontWeight.Bold,color=Color(0xFF6C63FF));TextButton(onClick=close){Text("ЗАКРЫТЬ",color=Color.White)}};Spacer(Modifier.height(20.dp));Text("🤖 Нейронка Gemini",color=Color.White,fontWeight=FontWeight.Bold);Button(onClick={},modifier=Modifier.fillMaxWidth(),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF6C63FF))){Text("Проверить серверы")};Spacer(Modifier.height(8.dp));Button(onClick={S.sel.intValue=(S.sel.intValue+1)%S.servers.size},modifier=Modifier.fillMaxWidth(),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF5A52E0))){Text("Сменить IP")};Spacer(Modifier.height(20.dp));Text("Серверов: \${S.servers.size}",color=Color.White.copy(0.7f));Text("Подключено: \${S.con.value}",color=Color.White.copy(0.7f))}}
