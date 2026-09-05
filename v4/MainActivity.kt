package com.physiovertex.app

import android.app.Activity
import android.os.Bundle
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity() {
    // Employee Records: legacy build verification token
    private val navy = Color.rgb(19,42,76)
    private val blue = Color.rgb(36,105,181)
    private val pale = Color.rgb(244,248,253)
    private val ink = Color.rgb(42,55,72)
    private val line = Color.rgb(218,229,242)
    private val prefs by lazy { getSharedPreferences("physiovertex", MODE_PRIVATE) }
    private lateinit var root: LinearLayout
    private var currentEmployee = "PV001"

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        if (!prefs.contains("emp_PV001_pass")) {
            prefs.edit()
                .putString("emp_PV001_name", "Physio 1")
                .putString("emp_PV001_pass", "1234")
                .putString("emp_PV001_salary", "0")
                .putString("emp_PV001_joined", today())
                .putString("salaryhist_PV001_${System.currentTimeMillis()}", "${today()}|0")
                .apply()
        }
        if (prefs.getString("emp_PV001_name", "") == "Physio 1") prefs.edit().putString("emp_PV001_name", "Physio 1").apply()
        if (prefs.getString("emp_PV001_name", "") == "Employee 1") prefs.edit().putString("emp_PV001_name", "Physio 1").apply()
        role()
    }

    private fun bg(c:Int,r:Float=24f,s:Int?=null)=GradientDrawable().apply{setColor(c);cornerRadius=r;if(s!=null)setStroke(2,s)}
    private fun base(center:Boolean=false):LinearLayout{val s=ScrollView(this).apply{setBackgroundColor(pale);isFillViewport=true};root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(38,28,38,46);if(center)gravity=Gravity.CENTER_VERTICAL};s.addView(root,ViewGroup.LayoutParams(-1,-1));setContentView(s);return root}
    private fun brand(v:LinearLayout,sub:String){val b=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setPadding(18,10,18,14);background=bg(Color.WHITE,28f,line);elevation=4f};b.addView(ImageView(this).apply{setImageResource(R.drawable.physiovertex_logo);adjustViewBounds=true;scaleType=ImageView.ScaleType.CENTER_INSIDE},LinearLayout.LayoutParams(-1,125));b.addView(TextView(this).apply{text=sub.uppercase();textSize=11f;letterSpacing=.09f;setTypeface(null,Typeface.BOLD);setTextColor(navy);gravity=Gravity.CENTER});v.addView(b,LinearLayout.LayoutParams(-1,-2));v.addView(sp(18))}
    private fun title(t:String)=TextView(this).apply{text=t.uppercase();textSize=23f;letterSpacing=.04f;setTextColor(navy);setTypeface(null,Typeface.BOLD);gravity=Gravity.CENTER;setPadding(4,8,4,18)}
    private fun label(t:String)=TextView(this).apply{text=t;textSize=16f;setTextColor(ink);setPadding(24,20,24,20);background=bg(Color.WHITE,22f,line);elevation=2f}
    private fun edit(h:String,p:Boolean=false)=EditText(this).apply{hint=h.uppercase();textSize=15f;setTextColor(ink);setHintTextColor(Color.rgb(116,132,151));setPadding(22,8,22,8);background=bg(Color.WHITE,18f,line);if(p)inputType=InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD;layoutParams=LinearLayout.LayoutParams(-1,60).apply{setMargins(0,7,0,7)}}
    private fun btn(t:String,a:()->Unit)=Button(this).apply{text=t.uppercase();textSize=14f;letterSpacing=.05f;setTextColor(Color.WHITE);setTypeface(null,Typeface.BOLD);background=bg(blue,24f);elevation=5f;minHeight=60;setOnClickListener{a()}}
    private fun menu(t:String,sub:String,a:()->Unit)=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(24,15,24,15);background=bg(Color.WHITE,22f,line);elevation=4f;addView(TextView(this@MainActivity).apply{text=t.uppercase();textSize=15f;letterSpacing=.04f;setTypeface(null,Typeface.BOLD);setTextColor(blue)});addView(TextView(this@MainActivity).apply{text=sub;textSize=12f;setTextColor(Color.rgb(103,119,139));setPadding(0,4,0,0)});setOnClickListener{a()}}
    private fun backBtn(t:String="Back",a:()->Unit)=Button(this).apply{text=t.uppercase();textSize=14f;setTextColor(navy);setTypeface(null,Typeface.BOLD);background=bg(Color.WHITE,24f,blue);elevation=2f;minHeight=56;setOnClickListener{a()}}
    private fun sp(h:Int=18)=Space(this).apply{layoutParams=LinearLayout.LayoutParams(1,h)}
    private fun today()=SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(Date())
    private fun ids()=prefs.all.keys.filter{it.startsWith("emp_")&&it.endsWith("_pass")}.map{it.removePrefix("emp_").removeSuffix("_pass")}.distinct().sorted()
    private fun archivedIds()=prefs.all.keys.filter{it.startsWith("archived_")&&it.endsWith("_name")}.map{it.removePrefix("archived_").removeSuffix("_name")}.distinct().sorted()
    private fun recordIds()=(ids()+archivedIds()).distinct().sorted()

    private fun role(){val v=base(true);brand(v,"Physio Attendance & Management");v.addView(title("Team Portal"));v.addView(btn("Physio Login"){empLogin()});v.addView(sp());v.addView(btn("Admin Login"){adminLogin()});v.addView(sp());v.addView(backBtn("Update App"){openLatestUpdate()})}
    private fun empLogin(){val v=base(true);brand(v,"Physio Portal");v.addView(title("Physio Login"));val id=edit("Physio ID");val p=edit("Password",true);v.addView(id);v.addView(p);v.addView(sp());v.addView(btn("Login"){val e=id.text.toString().trim().uppercase();if(prefs.getString("emp_${e}_pass",null)==p.text.toString()){currentEmployee=e;empDash()}else Toast.makeText(this,"Invalid login or profile not activated",Toast.LENGTH_SHORT).show()});v.addView(sp(10));v.addView(backBtn("Create Profile / Register"){registerPhysio()});v.addView(sp());v.addView(backBtn{role()})}
    private fun openLatestUpdate(){
        try{startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://github.com/radhejangid797-prog/Physiovertex-employee-/releases/download/latest/PhysioVertex-Latest.apk"))}
        catch(e:Exception){Toast.makeText(this,"Unable to open update link",Toast.LENGTH_SHORT).show()}
    }

    private fun registerPhysio(){
        val v=base();brand(v,"New Physio Registration");v.addView(title("Create Profile"))
        val n=edit("Full Name");val m=edit("Mobile Number");m.inputType=InputType.TYPE_CLASS_PHONE
        val e=edit("Email");e.inputType=InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        val q=edit("Qualification");val a=edit("Address");val pass=edit("Create Password",true)
        v.addView(n);v.addView(m);v.addView(e);v.addView(q);v.addView(a);v.addView(pass);v.addView(sp(12))
        v.addView(btn("Submit Profile"){
            val name=n.text.toString().trim();val mobile=m.text.toString().trim();val password=pass.text.toString()
            if(name.isBlank()||mobile.isBlank()||password.isBlank()) Toast.makeText(this,"Name, mobile and password required",Toast.LENGTH_SHORT).show()
            else {val k="reg_${System.currentTimeMillis()}";prefs.edit().putString(k,"$name|$mobile|${e.text.toString().trim()}|${q.text.toString().trim()}|${a.text.toString().trim()}|$password|PENDING").apply();Toast.makeText(this,"Profile submitted. Wait for Admin activation.",Toast.LENGTH_LONG).show();empLogin()}
        });v.addView(sp());v.addView(backBtn{empLogin()})
    }

    private fun adminLogin(){val v=base(true);brand(v,"Administration");v.addView(title("Admin Login"));val id=edit("Admin ID");val p=edit("Password",true);v.addView(id);v.addView(p);v.addView(sp());v.addView(btn("Login"){if(id.text.toString().trim().equals("ADMIN",true)&&p.text.toString()=="2468")adminDash()else Toast.makeText(this,"Invalid Admin Login",Toast.LENGTH_SHORT).show()});v.addView(sp());v.addView(backBtn{role()})}

    private fun adminDash(){
        val v=base();brand(v,"PhysioVertex Team Portal");v.addView(title("Admin Dashboard"))
        v.addView(menu("My Physio Dashboard","Personal attendance & physio view"){currentEmployee="PV001";empDash()});v.addView(sp(12))
        v.addView(menu("Pending Registrations","Review and activate new Physio profiles"){pendingRegistrations()});v.addView(sp(12));v.addView(menu("Add / Update Physio","Create and manage staff profiles"){editor()});v.addView(sp(12))
        v.addView(menu("Physio Records","Admin-only salary, work and service history"){employeeRecords()});v.addView(sp(12))
        v.addView(menu("Physio List / Delete","View your complete staff directory"){empList()});v.addView(sp(12))
        v.addView(menu("Attendance Overview","Today’s check-in, check-out & selfies"){attendance()});v.addView(sp(12))
        v.addView(menu("Leave Requests","Review pending physio requests"){adminLeaves()});v.addView(sp(20))
        v.addView(backBtn("Logout"){role()})
    }

    private fun pendingRegistrations(){
        val v=base();brand(v,"Physio Registrations");v.addView(title("Pending Registrations"))
        val pending=prefs.all.keys.filter{it.startsWith("reg_") && (prefs.getString(it,"")?:"").endsWith("|PENDING")}.sorted()
        if(pending.isEmpty()) v.addView(label("No pending registrations"))
        pending.forEach{k->
            val x=(prefs.getString(k,"")?:"").split("|",limit=7)
            val name=x.getOrElse(0){""};val mobile=x.getOrElse(1){""};val email=x.getOrElse(2){""};val qual=x.getOrElse(3){""};val address=x.getOrElse(4){""};val password=x.getOrElse(5){""}
            v.addView(label("$name\nMobile: $mobile\nEmail: $email\nQualification: $qual\nAddress: $address"));v.addView(sp(8))
            v.addView(btn("Approve & Create Physio ID"){
                val used=ids().mapNotNull{it.removePrefix("PV").toIntOrNull()}.toSet();var num=1;while(used.contains(num))num++;val id="PV"+num.toString().padStart(3,'0')
                if(ids().size>=10) Toast.makeText(this,"Maximum 10 physios allowed",Toast.LENGTH_SHORT).show()
                else {prefs.edit().putString("emp_${id}_name",name).putString("emp_${id}_pass",password).putString("emp_${id}_salary","0").putString("emp_${id}_joined",today()).putString("profile_${id}_name",name).putString("profile_${id}_mobile",mobile).putString("profile_${id}_email",email).putString("profile_${id}_qualification",qual).putString("profile_${id}_address",address).putString(k,(prefs.getString(k,"")?:"").removeSuffix("PENDING")+"APPROVED:$id").apply();Toast.makeText(this,"Activated: $id",Toast.LENGTH_LONG).show();pendingRegistrations()}
            });v.addView(sp(8));v.addView(backBtn("Reject"){prefs.edit().putString(k,(prefs.getString(k,"")?:"").removeSuffix("PENDING")+"REJECTED").apply();pendingRegistrations()});v.addView(sp(16))
        }
        v.addView(backBtn{adminDash()})
    }

    private fun empDash(){val v=base();brand(v,"Physio Attendance");val name=prefs.getString("emp_${currentEmployee}_name",currentEmployee)?:currentEmployee;v.addView(title("Welcome, $name"));val d=today();val i=prefs.getString("in_${currentEmployee}_$d",null);val o=prefs.getString("out_${currentEmployee}_$d",null);v.addView(label("PHYSIO ID  •  $currentEmployee\n\nTODAY'S ATTENDANCE\nCheck-In   ${i?:"Not marked"}\nCheck-Out  ${o?:"Not marked"}"));v.addView(sp(14));v.addView(btn("Check In with Selfie"){if(i!=null)Toast.makeText(this,"Already checked in",Toast.LENGTH_SHORT).show()else camera("IN")});v.addView(sp(10));v.addView(btn("Check Out with Selfie"){if(i==null)Toast.makeText(this,"Check in first",Toast.LENGTH_SHORT).show()else if(o!=null)Toast.makeText(this,"Already checked out",Toast.LENGTH_SHORT).show()else camera("OUT")});v.addView(sp(18));v.addView(menu("Attendance History","View your last 30 days attendance"){history()});v.addView(sp(10));v.addView(menu("Salary Details","View monthly salary information"){salary()});v.addView(sp(10));v.addView(menu("Leave Request","Submit a new leave request"){leave()});v.addView(sp(20));v.addView(backBtn("Logout"){role()})}

    private fun editor(){
        val v=base();brand(v,"Staff Management");v.addView(title("Add / Update Physio"))
        val id=edit("Physio ID e.g. PV002");val n=edit("Physio Name");val p=edit("Password",true);val sal=edit("Monthly Salary");sal.inputType=InputType.TYPE_CLASS_NUMBER
        v.addView(id);v.addView(n);v.addView(p);v.addView(sal);v.addView(sp())
        v.addView(btn("Save Physio"){
            val e=id.text.toString().trim().uppercase();val name=n.text.toString().trim();val pass=p.text.toString();val newSalary=sal.text.toString().ifBlank{"0"}
            if(e.isBlank()||name.isBlank()||pass.isBlank()) Toast.makeText(this,"ID, Name and Password required",Toast.LENGTH_SHORT).show()
            else if(!prefs.contains("emp_${e}_pass")&&ids().size>=10) Toast.makeText(this,"Maximum 10 physios allowed",Toast.LENGTH_SHORT).show()
            else {
                val oldSalary=prefs.getString("emp_${e}_salary",null)
                val ed=prefs.edit().putString("emp_${e}_name",name).putString("emp_${e}_pass",pass).putString("emp_${e}_salary",newSalary)
                if(!prefs.contains("emp_${e}_joined")) ed.putString("emp_${e}_joined",today())
                if(oldSalary==null || oldSalary!=newSalary) ed.putString("salaryhist_${e}_${System.currentTimeMillis()}","${today()}|$newSalary")
                ed.remove("archived_${e}_name").remove("archived_${e}_salary").remove("archived_${e}_joined").remove("archived_${e}_left").apply()
                adminDash()
            }
        });v.addView(sp());v.addView(backBtn{adminDash()})
    }

    private fun archiveEmployee(id:String){
        val name=prefs.getString("emp_${id}_name",id)?:id
        val salary=prefs.getString("emp_${id}_salary","0")?:"0"
        val joined=prefs.getString("emp_${id}_joined","")?:""
        prefs.edit()
            .putString("archived_${id}_name",name)
            .putString("archived_${id}_salary",salary)
            .putString("archived_${id}_joined",joined)
            .putString("archived_${id}_left",today())
            .remove("emp_${id}_name").remove("emp_${id}_pass").remove("emp_${id}_salary")
            .apply()
    }

    private fun empList(){val v=base();brand(v,"Staff Directory");v.addView(title("Physio List"));ids().forEach{id->v.addView(label("$id  •  ${prefs.getString("emp_${id}_name","")}\nMonthly Salary: ₹${prefs.getString("emp_${id}_salary","0")}"));v.addView(sp(10));if(id!="PV001"){v.addView(backBtn("Archive $id"){archiveEmployee(id);empList()});v.addView(sp())}};v.addView(backBtn{adminDash()})}

    private fun employeeRecords(){
        val v=base();brand(v,"Private Admin Records");v.addView(title("Physio Records"))
        val all=recordIds()
        if(all.isEmpty()) v.addView(label("No physio records"))
        all.forEach{id->
            val active=prefs.contains("emp_${id}_pass")
            val name=if(active) prefs.getString("emp_${id}_name",id) else prefs.getString("archived_${id}_name",id)
            val status=if(active) "ACTIVE" else "ARCHIVED"
            v.addView(menu("$id • ${name?:id}","$status • Open salary, work and attendance record"){employeeRecord(id)})
            v.addView(sp(10))
        }
        v.addView(backBtn{adminDash()})
    }

    private fun employeeRecord(id:String){
        val v=base();brand(v,"Admin Only Physio Record")
        val active=prefs.contains("emp_${id}_pass")
        val name=(if(active)prefs.getString("emp_${id}_name",id) else prefs.getString("archived_${id}_name",id))?:id
        val salary=(if(active)prefs.getString("emp_${id}_salary","0") else prefs.getString("archived_${id}_salary","0"))?:"0"
        val joined=(prefs.getString("emp_${id}_joined",null)?:prefs.getString("archived_${id}_joined",""))?:""
        val left=if(active) "-" else (prefs.getString("archived_${id}_left","-")?:"-")

        var days=0
        var totalMinutes=0L
        prefs.all.keys.filter{it.startsWith("inms_${id}_")}.forEach{k->
            val date=k.removePrefix("inms_${id}_")
            val im=prefs.getLong(k,0L)
            val om=prefs.getLong("outms_${id}_$date",0L)
            if(im>0){days++;if(om>=im)totalMinutes+=(om-im)/60000}
        }
        val leaveKeys=prefs.all.keys.filter{it.startsWith("leave_${id}_")}
        val approved=leaveKeys.count{(prefs.getString(it,"")?:"").endsWith("|APPROVED")}
        val pending=leaveKeys.count{(prefs.getString(it,"")?:"").endsWith("|PENDING")}
        val rejected=leaveKeys.count{(prefs.getString(it,"")?:"").endsWith("|REJECTED")}

        v.addView(title(name))
        v.addView(label("PHYSIO ID: $id\nSTATUS: ${if(active)"ACTIVE" else "ARCHIVED"}\nJOINED: ${joined.ifBlank{"Not recorded"}}\nLEFT: $left\n\nCURRENT / LAST SALARY: ₹$salary\n\nATTENDANCE DAYS: $days\nTOTAL WORKED: ${totalMinutes/60}h ${totalMinutes%60}m\n\nLEAVES — Approved: $approved  Pending: $pending  Rejected: $rejected"))
        v.addView(sp(14))

        v.addView(title("Salary History"))
        val salaryKeys=prefs.all.keys.filter{it.startsWith("salaryhist_${id}_")}.sortedDescending()
        if(salaryKeys.isEmpty()) v.addView(label("Current / last recorded salary: ₹$salary"))
        else salaryKeys.forEach{k->
            val p=(prefs.getString(k,"")?:"").split("|",limit=2)
            v.addView(label("${p.getOrElse(0){""}}  •  ₹${p.getOrElse(1){"0"}}"));v.addView(sp(8))
        }

        v.addView(sp(10));v.addView(title("Clinic Work Notes"))
        val noteKeys=prefs.all.keys.filter{it.startsWith("work_${id}_")}.sortedDescending()
        if(noteKeys.isEmpty()) v.addView(label("No work notes added yet"))
        else noteKeys.take(20).forEach{k->v.addView(label(prefs.getString(k,"")?:""));v.addView(sp(8))}
        val note=edit("Add clinic work / performance note")
        v.addView(note);v.addView(sp(10));v.addView(btn("Save Admin Note"){
            val text=note.text.toString().trim()
            if(text.isBlank()) Toast.makeText(this,"Enter a note",Toast.LENGTH_SHORT).show()
            else {val stamp=SimpleDateFormat("yyyy-MM-dd hh:mm a",Locale.getDefault()).format(Date());prefs.edit().putString("work_${id}_${System.currentTimeMillis()}","$stamp\n$text").apply();employeeRecord(id)}
        })
        v.addView(sp(16));v.addView(backBtn{employeeRecords()})
    }

    private fun camera(type:String){try{val x=Intent(MediaStore.ACTION_IMAGE_CAPTURE);x.putExtra("android.intent.extras.CAMERA_FACING",1);x.putExtra("android.intent.extra.USE_FRONT_CAMERA",true);startActivityForResult(x,if(type=="IN")501 else 502)}catch(e:Exception){Toast.makeText(this,"Camera unavailable",Toast.LENGTH_SHORT).show()}}
    override fun onActivityResult(req:Int,res:Int,data:Intent?){super.onActivityResult(req,res,data);if(res!=RESULT_OK)return;val bmp=data?.extras?.get("data") as? Bitmap?:return;val type=if(req==501)"IN" else if(req==502)"OUT" else return;val d=today();val f=File(filesDir,"selfie_${currentEmployee}_${d}_${type}.jpg");FileOutputStream(f).use{bmp.compress(Bitmap.CompressFormat.JPEG,90,it)};val t=SimpleDateFormat("hh:mm a",Locale.getDefault()).format(Date());val e=prefs.edit().putString("selfie_${type.lowercase()}_${currentEmployee}_$d",f.absolutePath);if(type=="IN")e.putString("in_${currentEmployee}_$d",t).putLong("inms_${currentEmployee}_$d",System.currentTimeMillis())else e.putString("out_${currentEmployee}_$d",t).putLong("outms_${currentEmployee}_$d",System.currentTimeMillis());e.apply();empDash()}
    private fun image(p:String?):ImageView?{if(p.isNullOrBlank()||!File(p).exists())return null;return ImageView(this).apply{setImageBitmap(BitmapFactory.decodeFile(p));adjustViewBounds=true;maxHeight=450}}
    private fun attendance(){val v=base();brand(v,"Attendance Control");v.addView(title("Attendance Overview"));val d=today();ids().forEach{id->val im=prefs.getLong("inms_${id}_$d",0);val om=prefs.getLong("outms_${id}_$d",0);val w=if(im>0&&om>=im){val m=(om-im)/60000;"${m/60}h ${m%60}m"}else "-";v.addView(label("$id  •  ${prefs.getString("emp_${id}_name","")}\nIN: ${prefs.getString("in_${id}_$d",null)?:"-"}   OUT: ${prefs.getString("out_${id}_$d",null)?:"-"}\nWorked: $w"));image(prefs.getString("selfie_in_${id}_$d",null))?.let{v.addView(it)};image(prefs.getString("selfie_out_${id}_$d",null))?.let{v.addView(it)};v.addView(sp())};v.addView(backBtn{adminDash()})}
    private fun leave(){val v=base();brand(v,"Physio Leave");v.addView(title("Leave Request"));val d=edit("Leave date");val r=edit("Reason");v.addView(d);v.addView(r);v.addView(sp());v.addView(btn("Submit Request"){if(d.text.isBlank()||r.text.isBlank())Toast.makeText(this,"Date and reason required",Toast.LENGTH_SHORT).show()else{prefs.edit().putString("leave_${currentEmployee}_${System.currentTimeMillis()}","${d.text}|${r.text}|PENDING").apply();empDash()}});v.addView(sp());v.addView(backBtn{empDash()})}
    private fun leaves()=prefs.all.keys.filter{it.startsWith("leave_")}.sortedDescending()
    private fun adminLeaves(){val v=base();brand(v,"Leave Management");v.addView(title("Leave Requests"));if(leaves().isEmpty())v.addView(label("No leave requests"));leaves().forEach{k->val p=(prefs.getString(k,"")?:"").split("|");val id=k.removePrefix("leave_").substringBefore("_");val d=p.getOrElse(0){""};val r=p.getOrElse(1){""};val st=p.getOrElse(2){"PENDING"};val name=prefs.getString("emp_${id}_name",null)?:prefs.getString("archived_${id}_name",id);v.addView(label("$id • $name\nDate: $d\nReason: $r\nStatus: $st"));if(st=="PENDING"){v.addView(sp(8));v.addView(btn("Approve"){prefs.edit().putString(k,"$d|$r|APPROVED").apply();adminLeaves()});v.addView(sp(8));v.addView(backBtn("Reject"){prefs.edit().putString(k,"$d|$r|REJECTED").apply();adminLeaves()})};v.addView(sp())};v.addView(backBtn{adminDash()})}
    private fun history(){val v=base();brand(v,"Attendance Records");v.addView(title("Attendance History"));val df=SimpleDateFormat("yyyy-MM-dd",Locale.getDefault());val c=Calendar.getInstance();repeat(30){val k=df.format(c.time);val i=prefs.getString("in_${currentEmployee}_$k",null);val o=prefs.getString("out_${currentEmployee}_$k",null);if(i!=null||o!=null){v.addView(label("$k\nIN: ${i?:"-"}   OUT: ${o?:"-"}"));v.addView(sp(10))};c.add(Calendar.DAY_OF_YEAR,-1)};v.addView(backBtn{empDash()})}
    private fun salary(){val v=base();brand(v,"Physio Payroll");v.addView(title("Salary Details"));v.addView(label("Physio: ${prefs.getString("emp_${currentEmployee}_name",currentEmployee)}\nID: $currentEmployee\nMonthly Salary: ₹${prefs.getString("emp_${currentEmployee}_salary","0")}"));v.addView(sp());v.addView(backBtn{empDash()})}
}
