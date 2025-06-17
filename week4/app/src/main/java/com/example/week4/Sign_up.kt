package com.example.week4

import com.example.week4.databinding.ActivityMainBinding


// create a copy from view binding class
private lateinit var binding_object_1 : ActivityMainBinding


/*
* variables : var and val
* val : constant : immutable : name , Family name...
* var : mutable : can be changed : age, time ,temp
*
* declare : creating the variable
* initialize : we are giving initial value to the variable
* val : declare + initialize at the same time ( mandatory)
* var : you can initialize later ( condition)
*
*
* View Binding class : is used to connect xml file with the kotlin file
* The connection is already built ( using the R-class) but we build a better one
* steps:
* ask the permission to use this connection
* call the view binding class and create a copy from it
* use this copy to access the xml components ( views) through the kotlin file
* */