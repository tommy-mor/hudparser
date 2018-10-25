"Resource/HudLayout.res"
{
	//--------------------------------------------------------------
	// Set visible and enabled to 1 to use.
	// Change xpos and ypos values if not perfectly centered.
	//--------------------------------------------------------------
	// RAYSHUD CROSSHAIRS
	//--------------------------------------------------------------
	"RaysCrosshair"
	{
		"controlName"	"CExLabel"
		"fieldName"	 	"RaysCrosshair"
		"visible"		"0"
		"enabled"		"0"
		"zpos"			"2"
		//test
		"xpos"			"c-85"
		"ypos"			"c-100" //test
		"wide"			"200"
				"fillcolor"		"255 255 255 255"
		"tall"			"200"
		"font"			"Crosshairs24"
		"labelText"		"i"
		"textAlignment"	"center"
		"fgcolor"		"Crosshair"
	}


    //--------------------------------------------------------------
    // KONRWINGS
    //--------------------------------------------------------------
    "KonrWings"
    {
    	"controlName"	"CExLabel"
    	"fieldName"	 	"KonrWings"
    	"visible"		"0"
    	"enabled"		"0"
    	"zpos"			"2"
    	"xpos"			"c-108"
    	"ypos"			"c-99"
    	"wide"			"200"
    	"tall"			"200"
    	"font"			"KonrWings24"
    	"labelText"		"i"
    	"textAlignment"	"center"
    	"fgcolor"		"Crosshair"

    }

    HudItemEffectMeter
    {
    	"fieldName"			"HudItemEffectMeter"
    	"visible"			"1"
    	"enabled"			"1"
    	"xpos"				"c-75"	[$WIN32]
    	"ypos"				"c25"	[$WIN32]
    	"wide"				"500" [$rts]
    	"tall"				"500"
    	"MeterFG"			"White"
    	"MeterBG"			"Gray"
    	if_mvm{
    	 "srtst" "strstr"
    	}

    		HudControlPointIcons
        	{
        		"fieldName"			"HudControlPointIcons"
        		"xpos"				"0"
        		"ypos"				"410"
        		"wide"				"f0"
        		"tall"				"200"
        		"visible"			"1"
        		"enabled"			"1"
        		"separator_width"	"3"				// distance between the icons (including their backgrounds)
        		"separator_height"	"3"
        		"height_offset"		"3"	[$WIN32]	// distance from the bottom of the panel
        	}
    }

	HUDAutoAim			{ }
	HudHDRDemo			{ }
	HudTrainingMsg		{ }
	TrainingComplete	{ }
	AnnotationsPanel	{ }
}
