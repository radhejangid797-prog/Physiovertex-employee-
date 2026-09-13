import sys

p=sys.argv[1]
s=open(p,encoding='utf-8').read()

old='''        v.addView(menu("My Profile","Contact and professional details"){physioProfile()});v.addView(sp(18))
        employeeBottomNav(v,"HOME")'''
new='''        v.addView(menu("My Profile","Contact and professional details"){physioProfile()});v.addView(sp(10))
        v.addView(menu("Notes","Save your personal work notes"){notesScreen()});v.addView(sp(10))
        v.addView(menu("Holiday List","View clinic holiday information"){holidayListScreen()});v.addView(sp(10))
        v.addView(menu("Documents","View your clinic documents section"){documentsScreen()});v.addView(sp(18))
        employeeBottomNav(v,"HOME")'''
if old not in s: raise SystemExit('Quick access anchor missing')
s=s.replace(old,new,1)

anchor='''    private fun physioProfile(){'''
extra=r'''    private fun notesScreen(){
        val v=base();brand(v,"Notes");v.addView(title("My Notes"))
        val note=EditText(this).apply{hint="Write your note here";setText(prefs.getString("note_${currentEmployee}",""));minLines=5;gravity=Gravity.TOP;setPadding(18,16,18,16);background=bg(Color.WHITE,18f,line);setTextColor(navy);setHintTextColor(gray)}
        v.addView(note,LinearLayout.LayoutParams(-1,-2));v.addView(sp(12))
        v.addView(btn("Save Note"){prefs.edit().putString("note_${currentEmployee}",note.text.toString()).apply();Toast.makeText(this,"Note saved",Toast.LENGTH_SHORT).show()})
        v.addView(sp(18));v.addView(backBtn("Back to Home"){empDash()})
    }

    private fun holidayListScreen(){
        val v=base();brand(v,"Holiday List");v.addView(title("Clinic Holidays"))
        val holidays=prefs.getString("clinic_holidays","")?:""
        if(holidays.isBlank())v.addView(label("No clinic holidays have been added yet.")) else v.addView(label(holidays))
        v.addView(sp(18));v.addView(backBtn("Back to Home"){empDash()})
    }

    private fun documentsScreen(){
        val v=base();brand(v,"Documents");v.addView(title("My Documents"))
        v.addView(label("Documents section is ready. Clinic policies, letters and staff documents can be added here in the next cloud-sync phase."))
        v.addView(sp(18));v.addView(backBtn("Back to Home"){empDash()})
    }

'''
if anchor not in s: raise SystemExit('Profile anchor missing')
s=s.replace(anchor,extra+anchor,1)
for token in ['Save Note','Clinic Holidays','My Documents']:
    if token not in s: raise SystemExit('Missing '+token)
open(p,'w',encoding='utf-8').write(s)
