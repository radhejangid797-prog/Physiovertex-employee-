import re,sys
p=sys.argv[1]
s=open(p,encoding='utf-8').read()
new=r'''private fun employeeBottomNav(v:LinearLayout,active:String){
        val nav=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER;setPadding(0,10,0,0)}
        fun item(symbol:String,labelText:String,key:String,action:()->Unit):LinearLayout{
            val selected=active==key
            return LinearLayout(this).apply{
                orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setPadding(2,6,2,6)
                background=if(selected)bg(blue,18f) else bg(Color.WHITE,18f,line)
                addView(TextView(this@MainActivity).apply{text=symbol;textSize=16f;gravity=Gravity.CENTER;setTextColor(if(selected)Color.WHITE else blue)})
                addView(TextView(this@MainActivity).apply{text=labelText;textSize=8.5f;gravity=Gravity.CENTER;setTypeface(null,Typeface.BOLD);setTextColor(if(selected)Color.WHITE else navy);maxLines=1})
                setOnClickListener{action()}
                layoutParams=LinearLayout.LayoutParams(0,64,1f).apply{setMargins(3,0,3,0)}
            }
        }
        nav.addView(item("⌂","HOME","HOME"){empDash()})
        nav.addView(item("✓","ATTEND","ATTEND"){attendanceCalendar()})
        nav.addView(item("L","LEAVE","LEAVE"){leave()})
        nav.addView(item("₹","SALARY","SALARY"){salary()})
        nav.addView(item("●","PROFILE","PROFILE"){physioProfile()})
        v.addView(nav,LinearLayout.LayoutParams(-1,-2))
    }

    private fun attendanceCalendar'''
pattern=r'private fun employeeBottomNav\(v:LinearLayout,active:String\)\{.*?\n    \}\n\n    private fun attendanceCalendar'
s2,n=re.subn(pattern,new,s,count=1,flags=re.S)
if n!=1: raise SystemExit('Bottom navigation function not found')
if '"HOME","HOME"' not in s2 or '"PROFILE","PROFILE"' not in s2: raise SystemExit('Navigation labels missing')
open(p,'w',encoding='utf-8').write(s2)
