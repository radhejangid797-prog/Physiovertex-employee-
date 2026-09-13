import sys

p = sys.argv[1]
s = open(p, encoding="utf-8").read()

# Existing source fixes and Build 56 markers.
s = s.replace(
    'try{startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://github.com/radhejangid797-prog/Physiovertex-employee-/releases/download/latest/PhysioVertex-Latest.apk"))}',
    'try{startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://github.com/radhejangid797-prog/Physiovertex-employee-/releases/download/latest/PhysioVertex-Latest.apk")))}'
)
s = s.replace('AlertDialog.Builder(this)', 'android.app.AlertDialog.Builder(this)')
s = s.replace('brand(v,"Physio Registrations")', 'brand(v,"Physio Registrations • Build 56")')
s = s.replace('.setTitle("Physio Approved")', '.setTitle("Physio Approved • Build 56")')

admin_anchor = 'v.addView(menu("Leave Requests","Review pending physio requests"){adminLeaves()});v.addView(sp(20))'
admin_new = 'v.addView(menu("Leave Requests","Review pending physio requests"){adminLeaves()});v.addView(sp(12))\n        v.addView(menu("Payroll Management","Set monthly salary, bonus and deduction"){payrollAdmin()});v.addView(sp(20))'
if admin_anchor not in s:
    raise SystemExit("Admin dashboard anchor missing")
s = s.replace(admin_anchor, admin_new, 1)

old_salary = 'private fun salary(){val v=base();brand(v,"Physio Payroll");v.addView(title("Salary Details"));v.addView(label("Physio: ${prefs.getString("emp_${currentEmployee}_name",currentEmployee)}\\nID: $currentEmployee\\nMonthly Salary: ₹${prefs.getString("emp_${currentEmployee}_salary","0")}"));v.addView(sp());v.addView(backBtn{empDash()})}'

new_salary = r'''private fun monthKey()=SimpleDateFormat("yyyy-MM",Locale.getDefault()).format(Date())
    private fun monthAttendanceDays(id:String):Int{val m=monthKey();return prefs.all.keys.count{it.startsWith("in_${id}_${m}-")}}

    private fun payrollAdmin(){
        val v=base();brand(v,"Payroll Management");v.addView(title("Monthly Payroll"))
        if(ids().isEmpty())v.addView(label("No active physios"))
        ids().forEach{id->
            val name=prefs.getString("emp_${id}_name",id)?:id
            val base=prefs.getString("emp_${id}_salary","0")?:"0"
            val x=(prefs.getString("payroll_${id}_${monthKey()}","")?:"").split("|")
            val bonus=x.getOrElse(0){"0"};val deduction=x.getOrElse(1){"0"}
            val payable=(base.toLongOrNull()?:0L)+(bonus.toLongOrNull()?:0L)-(deduction.toLongOrNull()?:0L)
            v.addView(menu("$id • $name","Base ₹$base • Payable ₹${payable.coerceAtLeast(0)}"){payrollEdit(id)});v.addView(sp(10))
        }
        v.addView(backBtn{adminDash()})
    }

    private fun payrollEdit(id:String){
        val v=base();brand(v,"Payroll Setup")
        val name=prefs.getString("emp_${id}_name",id)?:id;v.addView(title(name))
        val base=edit("Monthly Salary");base.inputType=InputType.TYPE_CLASS_NUMBER;base.setText(prefs.getString("emp_${id}_salary","0"))
        val x=(prefs.getString("payroll_${id}_${monthKey()}","")?:"").split("|")
        val bonus=edit("Bonus / Incentive");bonus.inputType=InputType.TYPE_CLASS_NUMBER;bonus.setText(x.getOrElse(0){"0"})
        val deduction=edit("Deduction");deduction.inputType=InputType.TYPE_CLASS_NUMBER;deduction.setText(x.getOrElse(1){"0"})
        v.addView(label("Month: ${monthKey()}\nAttendance marked: ${monthAttendanceDays(id)} days\nAttendance is for reference only; admin decides payroll amounts."));v.addView(sp(12))
        v.addView(base);v.addView(bonus);v.addView(deduction);v.addView(sp(12))
        v.addView(btn("Save Payroll"){
            val b=base.text.toString().toLongOrNull()?:0L
            val bo=bonus.text.toString().toLongOrNull()?:0L
            val de=deduction.text.toString().toLongOrNull()?:0L
            prefs.edit().putString("emp_${id}_salary",b.toString()).putString("payroll_${id}_${monthKey()}","$bo|$de").putString("salaryhist_${id}_${System.currentTimeMillis()}","${today()}|$b").apply()
            Toast.makeText(this,"Payroll saved",Toast.LENGTH_SHORT).show();payrollAdmin()
        });v.addView(sp());v.addView(backBtn{payrollAdmin()})
    }

    private fun salary(){
        val v=base();brand(v,"Physio Payroll");v.addView(title("Salary Details"))
        val base=(prefs.getString("emp_${currentEmployee}_salary","0")?:"0").toLongOrNull()?:0L
        val x=(prefs.getString("payroll_${currentEmployee}_${monthKey()}","")?:"").split("|")
        val bonus=x.getOrElse(0){"0"}.toLongOrNull()?:0L
        val deduction=x.getOrElse(1){"0"}.toLongOrNull()?:0L
        val payable=(base+bonus-deduction).coerceAtLeast(0)
        v.addView(label("Physio: ${prefs.getString("emp_${currentEmployee}_name",currentEmployee)}\nID: $currentEmployee\nMonth: ${monthKey()}\n\nMonthly Salary: ₹$base\nBonus / Incentive: ₹$bonus\nDeduction: ₹$deduction\n--------------------\nPayable Salary: ₹$payable\n\nAttendance marked: ${monthAttendanceDays(currentEmployee)} days"))
        v.addView(sp());v.addView(backBtn{empDash()})
    }'''

if old_salary not in s:
    raise SystemExit("Old salary function not found")
s = s.replace(old_salary, new_salary, 1)

if 'Activated: $id' in s:
    raise SystemExit("Old approval toast still present")
for token in ["Physio Approved • Build 56", "Payroll Management", "Payable Salary", "private fun payrollEdit"]:
    if token not in s:
        raise SystemExit("Missing " + token)

open(p, "w", encoding="utf-8").write(s)
