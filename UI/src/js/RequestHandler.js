  function createVM(){
	  
	  var vm_name = document.getElementById("vm_name").value;
	  var vm_type_id = document.getElementById("vm_type").value
	  var vm_image_id = document.getElementById("vm_image").value
	  
	  alert(vm_name+" "+vm_type_id+" "+vm_image_id);
	  
	  //localhost:10000/vm/create/?name=rohan&instance_type=1&image_id=0
	  var urli= 'http://localhost:10000/vm/create/?name='+vm_name+'&instance_type='+vm_type_id+'&image_id='+vm_image_id;
	  
	  $.ajax({
		  type: 'POST',
		  dataType:'jsonp',
		  contentType: 'application/json',
	   	  mimeType: 'application/json',
		  url: urli,
	  });
						  
  }
  function successF(resp){
    console.log(resp.vmid);
    if(resp.vmid != 0){
	  document.getElementById("success_message").innerHTML = "Virtual machine with id("+resp.vmid+") created successfully !!";
      document.getElementById("success_prompt").style.display="block";
      document.getElementById("error_prompt").style.display="none";
	}
    else{
	  document.getElementById("error_message").innerHTML = "Virtual machine could not be created. Please change the configuration !!";
      document.getElementById("error_prompt").style.display="block";
      document.getElementById("success_prompt").style.display="none";
    }
  }

  function queryVM(){
	  var vmid = document.getElementById("q_vm_id").value;
	  alert(vmid);
	  //http://localhost:10000/vm/query?vmid=0-14
	  var urli= 'http://localhost:10000/vm/query?vmid='+vmid;
	  $.ajax({
		  type: 'POST',
		  dataType:'jsonp',
		  contentType: 'application/json',
	   	  mimeType: 'application/json',
		  url: urli,
	  });
  }
  function showOutput(resp){
	  
	  if(resp.status == 0){
		  document.getElementById("q_error").style.display="block";
		  document.getElementById("q_success").style.display="none";
	  }
	  else{
		  document.getElementById("q_error").style.display="none";
		  document.getElementById("q_success").style.display="block";
		  document.getElementById("q_vmid").innerHTML ="<b>Virtual Machine ID : </b>"+resp.vmid;
		  document.getElementById("q_type").innerHTML ="<b>Name : </b>"+resp.name;
		  document.getElementById("q_name").innerHTML ="<b>Type : </b>"+resp.instance_type;
		  document.getElementById("q_pmid").innerHTML ="<b>Physical Machine ID : </b>"+resp.pmid;
	  }
  }
  
  
  function destroyVM(){
	  var vmid = document.getElementById("d_vm_id").value;
	  alert(vmid);
	  //http://localhost:10000/vm/d?vmid=0-14
	  var urli= 'http://localhost:10000/vm/destroy?vmid='+vmid;
	  $.ajax({
		  type: 'POST',
		  dataType:'jsonp',
		  contentType: 'application/json',
	   	  mimeType: 'application/json',
		  url: urli,
	  });
  }
  function successD(resp){
	  if(resp.status == 1){
	  document.getElementById("success_message").innerHTML = "Virtual machine destroyed successfully !!";
      document.getElementById("success_prompt").style.display="block";
      document.getElementById("error_prompt").style.display="none";
	}
    else{
	  document.getElementById("error_message").innerHTML = "Virtual machine could not be destroyed. Please check the vmid !!";
      document.getElementById("error_prompt").style.display="block";
      document.getElementById("success_prompt").style.display="none";
    }
  }
  
  
  function createStorage(){
    var storage_name = document.getElementById("storage_name").value;
    var storage_size = document.getElementById("storage_size").value
 	  	  
    alert(storage_name+" "+storage_size);
    
    //http://localhost:10000/volume/create/?name=test1&size=1
    var urli= 'http://localhost:10000/volume/create/?name='+storage_name+'&size='+storage_size;
    
    $.ajax({
      type: 'POST',
      dataType:'jsonp',
      contentType: 'application/json',
      mimeType: 'application/json',
      url: urli,
    });
  }
  function successSC(resp){
    console.log(resp.volumeid);
    if(resp.volumeid != 0){
      document.getElementById("success_message").innerHTML = "Storage with id("+resp.volumeid+") created successfully !!";
      document.getElementById("success_prompt").style.display="block";
      document.getElementById("error_prompt").style.display="none";
    }
    else{
      document.getElementById("error_message").innerHTML = "Desired storage could not be allocated. Please change the configuration !!";
      document.getElementById("error_prompt").style.display="block";
      document.getElementById("success_prompt").style.display="none";
    }
  }
  
  function queryST(){
      var stid = document.getElementById("q_st_id").value;
      alert(stid);
      http://localhost:10000/volume/query?volumeid=0
      var urli= 'http://localhost:10000/volume/query?volumeid='+stid;
      $.ajax({
	type: 'POST',
	dataType:'jsonp',
	contentType: 'application/json',
	mimeType: 'application/json',
	url: urli,
      });
    }
  function successSQ(resp){
      alert(resp.volumeid);
      if(resp.error){
	document.getElementById("q_st_error").style.display="block";
	document.getElementById("q_st_success").style.display="none";
      }
      else{
	document.getElementById("q_st_error").style.display="none";
	document.getElementById("q_st_success").style.display="block";
	
	
	document.getElementById("q_st_id_q").innerHTML ="<b>ID : </b>"+resp.volumeid;
	document.getElementById("q_st_name").innerHTML ="<b>Name : </b>"+resp.name;
	document.getElementById("q_st_size").innerHTML ="<b>Type : </b>"+resp.size;
	document.getElementById("q_st_status").innerHTML ="<b>Status : </b>"+resp.status;
	document.getElementById("q_st_vm_id").innerHTML ="<b>Virtual Machine ID : </b>"+resp.vmid;
	
      }
    }
  



