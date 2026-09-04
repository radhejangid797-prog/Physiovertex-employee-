package com.physiovertex.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.text.InputType
import android.view.Gravity
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity() {
    private val blue = Color.rgb(24, 92, 180)
    private val prefs by lazy { getSharedPreferences("physiovertex", MODE_PRIVATE) }
    private lateinit var root: LinearLayout
    private var currentEmployee = "PV001"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!prefs.contains("emp_PV001_pass")) prefs.edit().putString("emp_PV001_name", "Employee 1").putString("emp_PV001_pass", "1234").putString("emp_PV001_salary", "0").apply()
        showRoleScreen()
    }

    private fun base(): LinearLayout {
        val scroll = ScrollView(this)
        root = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(48,56,48,56); setBackgroundColor(Color.rgb(245,249,255)) }
        scroll.addView(root); setContentView(scroll); return root
    }
    private fun title(t:String)=TextView(this).apply{text=t;textSize=28f;setTextColor(blue);setTypeface(null,Typeface.BOLD);gravity=Gravity.CENTER;setPadding(0,20,0,28)}
    private fun label(t:String)=TextView(this).apply{text=t;textSize=17f;setTextColor(Color.DKGRAY);setPadding(4,14,4,8)}
    private fun edit(h:String,p:Boolean=false)=EditText(this).apply{hint=h;if(p)inputType=InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD}
    private fun button(t:String,a:()->Unit)=Button(this).apply{text=t;textSize=17f;setTextColor(Color.WHITE);setBackgroundColor(blue);setOnClickListener{a()}}
    private fun space()=Space(this).apply{layoutParams=LinearLayout.LayoutParams(1,22)}

    private fun showRoleScreen(){val v=base();v.addView(title("PhysioVertex v4"));v.addView(button("EMPLOYEE LOGIN"){showEmployeeLogin()});v.addView(space());v.addView(button("ADMIN LOGIN"){showAdminLogin()})}
    private fun showEmployeeLogin(){val v=base();v.addView(title("Employee Login"));val id=edit("Employee ID");val pass=edit("Password",true);v.addView(id);v.addView(pass);v.addView(button("LOGIN"){val e=id.text.toString().trim().uppercase(Locale.getDefault());if(prefs.getString("emp_${e}_pass",null)==pass.text.toString()){currentEmployee=e;showEmployeeDashboard()}else Toast.makeText(this,"Invalid Employee ID or Password",Toast.LENGTH_SHORT).show()});v.addView(space());v.addView(button("BACK"){showRoleScreen()})}
    private fun showAdminLogin(){val v=base();v.addView(title("Admin Login"));val id=edit("Admin ID");val pass=edit("Password",true);v.addView(id);v.addView(pass);v.addView(label("Default: ADMIN / 2468"));v.addView(button("LOGIN"){if(id.text.toString().trim().equals("ADMIN",true)&&pass.text.toString()=="2468")showAdminDashboard() else Toast.makeText(this,"Invalid Admin Login",Toast.LENGTH_SHORT).show()});v.addView(space());v.addView(button("BACK"){showRoleScreen()})}

    private fun showAdminDashboard(){val v=base();v.addView(title("Admin Dashboard v4 DIRECT"));v.addView(button("MY EMPLOYEE DASHBOARD"){currentEmployee="PV001";showEmployeeDashboard()});v.addView(space());v.addView(button("ADD / UPDATE EMPLOYEE"){showEmployeeEditor()});v.addView(space());v.addView(button("EMPLOYEE LIST"){showEmployeeList()});v.addView(space());v.addView(button("ATTENDANCE OVERVIEW"){showAdminAttendance()});v.addView(space());v.addView(button("LOGOUT"){showRoleScreen()})}

    private fun showEmployeeEditor(){val v=base();v.addView(title("Add / Update Employee"));val id=edit("Employee ID e.g. PV002");val name=edit("Employee Name");val pass=edit("Password",true);val salary=edit("Monthly Salary");salary.inputType=InputType.TYPE_CLASS_NUMBER;v.addView(id);v.addView(name);v.addView(pass);v.addView(salary);v.addView(button("SAVE EMPLOYEE"){val e=id.text.toString().trim().uppercase(Locale.getDefault());if(e.isBlank()||name.text.toString().isBlank()||pass.text.toString().isBlank())Toast.makeText(this,"ID, Name and Password required",Toast.LENGTH_SHORT).show() else if(!prefs.contains("emp_${e}_pass")&&employeeIds().size>=10)Toast.makeText(this,"Maximum 10 employees allowed",Toast.LENGTH_SHORT).show() else {prefs.edit().putString("emp_${e}_name",name.text.toString().trim()).putString("emp_${e}_pass",pass.text.toString()).putString("emp_${e}_salary",salary.text.toString().ifBlank{"0"}).apply();Toast.makeText(this,"Employee saved",Toast.LENGTH_SHORT).show();showAdminDashboard()}});v.addView(space());v.addView(button("BACK"){showAdminDashboard()})}
    private fun employeeIds()=prefs.all.keys.filter{it.startsWith("emp_")&&it.endsWith("_pass")}.map{it.removePrefix("emp_").removeSuffix("_pass")}.distinct().sorted()
    private fun showEmployeeList(){val v=base();v.addView(title("Employee List"));employeeIds().forEach{id->v.addView(label("$id - ${prefs.getString("emp_${id}_name","")}\nSalary: ₹${prefs.getString("emp_${id}_salary","0")}"))};v.addView(space());v.addView(button("BACK"){showAdminDashboard()})}

    private fun showEmployeeDashboard(){val v=base();val name=prefs.getString("emp_${currentEmployee}_name",currentEmployee);v.addView(title("Employee Dashboard"));v.addView(label("$name ($currentEmployee)"));v.addView(label("Attendance radius: 50 metres"));val status=label("");fun refresh(){val d=SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(Date());val i=prefs.getString("in_${currentEmployee}_$d",null);val o=prefs.getString("out_${currentEmployee}_$d",null);val im=prefs.getLong("inms_${currentEmployee}_$d",0);val om=prefs.getLong("outms_${currentEmployee}_$d",0);val worked=if(im>0&&om>=im){val m=(om-im)/60000;"${m/60}h ${m%60}m"}else "-";status.text="Today\nIN: ${i?:"Not marked"}\nOUT: ${o?:"Not marked"}\nWORKED: $worked"};v.addView(status);refresh();v.addView(button("CHECK IN"){val d=SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(Date());val t=SimpleDateFormat("hh:mm a",Locale.getDefault()).format(Date());prefs.edit().putString("in_${currentEmployee}_$d",t).putLong("inms_${currentEmployee}_$d",System.currentTimeMillis()).apply();refresh()});v.addView(space());v.addView(button("CHECK OUT"){val d=SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(Date());val t=SimpleDateFormat("hh:mm a",Locale.getDefault()).format(Date());prefs.edit().putString("out_${currentEmployee}_$d",t).putLong("outms_${currentEmployee}_$d",System.currentTimeMillis()).apply();refresh()});v.addView(space());v.addView(button("ATTENDANCE HISTORY"){showEmployeeHistory()});v.addView(space());v.addView(button("SALARY DETAILS"){showSalary()});v.addView(space());v.addView(button("LOGOUT"){showRoleScreen()})}
    private fun showEmployeeHistory(){val v=base();v.addView(title("Attendance History"));val df=SimpleDateFormat("yyyy-MM-dd",Locale.getDefault());val ds=SimpleDateFormat("dd MMM yyyy",Locale.getDefault());val cal=Calendar.getInstance();repeat(30){val k=df.format(cal.time);val i=prefs.getString("in_${currentEmployee}_$k",null);val o=prefs.getString("out_${currentEmployee}_$k",null);if(i!=null||o!=null)v.addView(label("${ds.format(cal.time)}\nIN: ${i?:"-"}   OUT: ${o?:"-"}"));cal.add(Calendar.DAY_OF_YEAR,-1)};v.addView(space());v.addView(button("BACK"){showEmployeeDashboard()})}
    private fun showSalary(){val v=base();v.addView(title("Salary Details"));v.addView(label("Employee: $currentEmployee"));v.addView(label("Monthly Salary: ₹${prefs.getString("emp_${currentEmployee}_salary","0")}"));v.addView(space());v.addView(button("BACK"){showEmployeeDashboard()})}
    private fun showAdminAttendance(){val v=base();v.addView(title("Attendance Overview"));val d=SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(Date());employeeIds().forEach{id->v.addView(label("$id - ${prefs.getString("emp_${id}_name","")}\nIN: ${prefs.getString("in_${id}_$d",null)?:"-"}   OUT: ${prefs.getString("out_${id}_$d",null)?:"-"}"))};v.addView(space());v.addView(button("BACK"){showAdminDashboard()})}
}
