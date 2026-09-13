import re,sys
p=sys.argv[1]
s=open(p,encoding='utf-8').read()
new=r'''private fun employeeBottomNav(v:LinearLayout,active:String){
        val density=resources.displayMetrics.density
        val navHeight=(72*density).toInt()
        val margin=(3*density).toInt()
        val padV=(5*density).toInt()
        val nav=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER;setPadding(0,(10*density).toInt(),0,0)}
        fun item(symbol:String,labelText:String,key:String,action:()->Unit):LinearLayout{
            val selected=active==key
            return LinearLayout(this).apply{
                orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setPadding(2,padV,2,padV)
                background=if(selected)bg(blue,18f) else bg(Color.WHITE,18f,line)
                addView(TextView(this@MainActivity).apply{text=symbol;textSize=17f;gravity=Gravity.CENTER;setTextColor(if(selected)Color.WHITE else blue);includeFontPadding=false})
                addView(TextView(this@MainActivity).apply{text=labelText;textSize=9f;gravity=Gravity.CENTER;setTypeface(null,Typeface.BOLD);setTextColor(if(selected)Color.WHITE else navy);maxLines=1;includeFontPadding=false})
                setOnClickListener{action()}
                layoutParams=LinearLayout.LayoutParams(0,navHeight,1f).apply{setMargins(margin,0,margin,0)}
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
for token in ['"HOME","HOME"','"ATTEND","ATTEND"','"LEAVE","LEAVE"','"SALARY","SALARY"','"PROFILE","PROFILE"','navHeight']:
    if token not in s2: raise SystemExit('Navigation token missing: '+token)
open(p,'w',encoding='utf-8').write(s2)
