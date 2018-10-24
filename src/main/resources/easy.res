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
		"tall"			"200"
		"font"			"Crosshairs24"
		"labelText"		"i"
		"textAlignment"	"center"
		"fgcolor"		"Crosshair"
	}

	HUDAutoAim			{ }
	HudHDRDemo			{ }
	HudTrainingMsg		{ }
	TrainingComplete	{ }
	AnnotationsPanel	{ }
}
