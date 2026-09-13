import sys

p=sys.argv[1]
s=open(p,encoding='utf-8').read()

old='private fun empDash(){val v=base();brand(v,"Physio Attendance");val name=prefs.getString("emp_${currentEmployee}_name",currentEmployee)?:currentEmployee;v.addView(title("Welcome, $name"));val d=today();val i=prefs.getString("in_${currentEmployee}_$d",null);val o=prefs.getString("out_${currentEmployee}_$d",null);v.addView(label("PHYSIO ID  •  $currentEmployee\\n\\nTODAY\'S ATTENDANCE\\nCheck-In   ${i?:"Not marked"}\\nCheck-Out  ${o?:"Not marked"}"));v.addView(sp(14));v.addView(btn("Check In with Selfie"){if(i!=null)Toast.makeText(this,"Already checked in",Toast.LENGTH_SHORT).show()else camera("IN")});v.addView(sp(10));v.addView(btn("Check Out with Selfie"){if(i==null)Toast.makeText(this,"Check in first",Toast.LENGTH_SHORT).show()else if(o!=null)Toast.makeText(this,"Already checked out",Toast.LENGTH_SHORT).show()else camera("OUT")});v.addView(sp(18));v.addView(menu("Attendance History","View your last 30 days attendance"){history()});v.addView(sp(10));v.addView(menu("Salary Details","View monthly salary information"){salary()});v.addView(sp(10));v.addView(menu("Leave Request","Submit a new leave request"){leave()});v.addView(sp(20));v.addView(backBtn("Logout"){role()})}'

new=r'''private fun empDash(){
        val v=base();brand(v,"PhysioVertex • Home")
        val name=prefs.getString("emp_${currentEmployee}_name",currentEmployee)?:currentEmployee
        val d=today();val i=prefs.getString("in_${currentEmployee}_$d",null);val o=prefs.getString("out_${currentEmployee}_$d",null)
        v.addView(title("Hi, $name"))
        v.addView(label("PHYSIO ID  •  $currentEmployee\nTODAY  •  $d\n\nCHECK-IN   ${i?:"Not marked"}\nCHECK-OUT  ${o?:"Not marked"}"))
        v.addView(sp(14))
        v.addView(btn("Check In with Selfie"){if(i!=null)Toast.makeText(this,"Already checked in",Toast.LENGTH_SHORT).show()else camera("IN")})
        v.addView(sp(10));v.addView(btn("Check Out with Selfie"){if(i==null)Toast.makeText(this,"Check in first",Toast.LENGTH_SHORT).show()else if(o!=null)Toast.makeText(this,"Already checked out",Toast.LENGTH_SHORT).show()else camera("OUT")})
        v.addView(sp(18));v.addView(title("Quick Access"))
        v.addView(menu("Attendance Calendar","Current month attendance at a glance"){attendanceCalendar()});v.addView(sp(10))
        v.addView(menu("Leave Request","Apply and review your leave requests"){leave()});v.addView(sp(10))
        v.addView(menu("Salary / Payroll","View monthly salary, bonus and deductions"){salary()});v.addView(sp(10))
        v.addView(menu("My Profile","Contact and professional details"){physioProfile()});v.addView(sp(18))
        employeeBottomNav(v,"HOME")
    }

    private fun employeeBottomNav(v:LinearLayout,active:String){
        val nav=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER;setPadding(0,8,0,0)}
        fun item(text:String,selected:Boolean,action:()->Unit)=Button(this).apply{text=textSize.let{text};this.text=text;this.textSize=10f;setTypeface(null,Typeface.BOLD);setTextColor(if(selected)Color.WHITE else navy);background=if(selected)bg(blue,18f) else bg(Color.WHITE,18f,line);setOnClickListener{action()};layoutParams=LinearLayout.LayoutParams(0,54,1f).apply{setMargins(3,0,3,0)}}
        nav.addView(item("HOME",active=="HOME"){empDash()});nav.addView(item("ATTEND",active=="ATTEND"){attendanceCalendar()});nav.addView(item("LEAVE",active=="LEAVE"){leave()});nav.addView(item("SALARY",active=="SALARY"){salary()});nav.addView(item("PROFILE",active=="PROFILE"){physioProfile()})
        v.addView(nav,LinearLayout.LayoutParams(-1,-2))
    }

    private fun attendanceCalendar(){
        val v=base();brand(v,"Attendance Calendar")
        val month=SimpleDateFormat("MMMM yyyy",Locale.getDefault()).format(Date());val prefix=SimpleDateFormat("yyyy-MM",Locale.getDefault()).format(Date())
        v.addView(title(month))
        val days=prefs.all.keys.filter{it.startsWith("in_${currentEmployee}_${prefix}-")}.map{it.removePrefix("in_${currentEmployee}_")}.sorted()
        if(days.isEmpty())v.addView(label("No attendance marked this month yet."))
        days.forEach{day->val checkIn=prefs.getString("in_${currentEmployee}_$day","Not marked")?:"Not marked";val checkOut=prefs.getString("out_${currentEmployee}_$day","Not marked")?:"Not marked";v.addView(label("$day\nCheck-In: $checkIn\nCheck-Out: $checkOut"));v.addView(sp(8))}
        v.addView(sp(10));v.addView(menu("Full Attendance History","View previous attendance records"){history()});v.addView(sp(18));employeeBottomNav(v,"ATTEND")
    }

    private fun physioProfile(){
        val v=base();brand(v,"My Profile")
        val name=prefs.getString("emp_${currentEmployee}_name",currentEmployee)?:currentEmployee;val mobile=prefs.getString("profile_${currentEmployee}_mobile","Not added")?:"Not added";val email=prefs.getString("profile_${currentEmployee}_email","Not added")?:"Not added";val qualification=prefs.getString("profile_${currentEmployee}_qualification","Not added")?:"Not added";val address=prefs.getString("profile_${currentEmployee}_address","Not added")?:"Not added";val joined=prefs.getString("emp_${currentEmployee}_joined","Not available")?:"Not available"
        v.addView(title(name));v.addView(label("PHYSIO ID  •  $currentEmployee\n\nMobile: $mobile\nEmail: $email\nQualification: $qualification\nAddress: $address\nJoining Date: $joined"));v.addView(sp(14));v.addView(backBtn("Logout"){role()});v.addView(sp(18));employeeBottomNav(v,"PROFILE")
    }'''

if old not in s: raise SystemExit('Old physio dashboard anchor missing')
s=s.replace(old,new,1)
for token in ['PhysioVertex • Home','Attendance Calendar','employeeBottomNav','My Profile']:
    if token not in s: raise SystemExit('Missing '+token)
open(p,'w',encoding='utf-8').write(s)
