<div class="site-wrapper">
    <div class="">
        <div class="cover-container">
            <!-- Menu -->
            <div class="masthead clearfix">
                <div class="">
                    <h3 class="masthead-brand"><img src="<?=img("logo.png")?>" height="60" alt="logo" /></h3>
                    <ul class="nav nav-pills pull-right" style="margin-top:20px;">
                        <li class="active"><a href="/admin/index">Accueil</a></li>
                        <li><a href="/home/price">Users</a></li>
                        <li><a href="/home/download">Plans</a></li>
                    </ul>
                </div>
            </div>

            <div class="inner cover admin">
               <h1>Gestion utilisateurs</h1>
               <div class="result"></div>
               <table class="table table-striped">
                <tr>
                    <th>#</th>
                    <th>Email</th>
                    <th>Plan</th>
                    <th>Admin ?</th>
                    <th>Actions</th>
                </tr>

               <?php 
               echo empty($users)?"<tr><td colspan='5' style='text-align:center'>Aucun utilisateur en base</td></tr>":"";
               foreach ($users as $user): ?>
                <tr>
                    <td><?php echo $user["id"]; ?></td>
                    <td><?php echo $user["email"]; ?></td>
                    <td><?php echo "hello" ?></td>
                    <td><?php echo $user["is_admin"]==true?"Oui":"Non"; ?></td>
                    <td id="<?php echo $user["id"]; ?>">
                        <button type="button" class="btn btn-xs btn-info editer" data-loading-text="Loading..."><span class="glyphicon glyphicon-pencil"></span>&nbsp; Editer</button> 
                         &nbsp; 
                        <button type="button" class="btn btn-xs btn-danger supprimer"><span class="glyphicon glyphicon-trash"></span>&nbsp; Supprimer</button>
                    </td>
                </tr>
               <?php endforeach; ?>
               </table>

               
            </div>

            <div class="mastfoot">
                <div class="inner">
                    <p>Cubbyhole powered baby !</p>
                </div>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    var deleteencours;
    $("button.supprimer").click(function(){
        if(deleteencours == true){
            alert("Doucement garçon... un edit à la fois !");
        }
        else{
            deleteencours = true;
            var userId = $(this).parent().attr('id');
            $.ajax({
                url: '/api/user/delete/'+userId,
                type: 'DELETE',
                headers:{
                    "X-API-KEY":"5422e102a743fd70a22ee4ff7c2ebbe8"
                },
                success: function(result) {
                    if(result.error == false){
                        $("div.result").append('<p class="bg-success" style="padding: 5px 0px;">'+result["message"]+'</p>');
                        $("td#"+userId).parent().remove();
                    }
                    else{
                        $("div.result").append('<p class="bg-danger" style="padding: 5px 0px;">'+result["message"]+'</p>');
                    }
                    deleteencours = false;
                },
                error: function(result) {
                        $("div.result").append('<p class="bg-danger" style="padding: 5px 0px;">Erreur lors du delete.</p>');
                        deleteencours = false;
                }
            });
        }
    });
    var editencours;
    $("button.editer").click(function(){
        if(editencours == true){
            alert("Doucement garçon... un edit à la fois !");
        }
        else{
            editencours = true;

            var btn = $(this);
            btn.button('loading');
            var userId = $(this).parent().attr('id');
            $.ajax({
                url: '/api/user/details/'+userId,
                type: 'GET',
                headers:{
                    "X-API-KEY":"5422e102a743fd70a22ee4ff7c2ebbe8"
                },
                success: function(result) {

                    if(result.id > 0){
                        $("#user_id").val(result.id);
                        $("#user_email").val(result.email);
                        if(result.isAdmin == true){
                            $("#user_is_admin_yes").prop("checked", true);
                            $("#user_is_admin_no").prop("checked", false);
                        }
                        else{
                            $("#user_is_admin_yes").prop("checked", false);
                            $("#user_is_admin_no").prop("checked", true);
                        }
                       
                        $("#editUser").modal("show");  
                        
                    }
                    else alert("bug api");
                }
            }).always(function() {
              btn.button('reset');
               editencours = false;
            });
        }
    });
</script>

<!-- Modal edit user -->
<div class="modal fade" id="editUser" tabindex="-1" role="dialog" aria-labelledby="Edit user" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h4 class="modal-title">Edit user </h4>
      </div>
      <form class="form-horizontal" role="form" method="post" id="formEditUser">
        <input type="hidden" id="user_id" name="user_id" value="" />
          <div class="modal-body">
              <div class="form-group">
                <label for="user_email" class="col-sm-2 control-label">Email</label>
                <div class="col-sm-10">
                  <input type="email" class="form-control" id="user_email" placeholder="Email" name="user_email" required />
                </div>
              </div>
              <div class="form-group">
                <label for="user_password" class="col-sm-2 control-label">Password</label>
                <div class="col-sm-10">
                  <input type="password" class="form-control" id="user_password" name="user_password" placeholder="Password (optionnel)" />
                </div>
              </div>
              <div class="form-group">
                <label for="user_password" class="col-sm-2 control-label">Privilèges</label>
                <div class="col-sm-10">
                    <label class="radio-inline">
                      <input type="radio" id="user_is_admin_yes" name="user_is_admin" value="1"> Administrateur
                    </label>
                    <label class="radio-inline">
                      <input type="radio" id="user_is_admin_no" name="user_is_admin" value="0"> Utilisateur
                    </label>
                </div>
              </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-default" data-dismiss="modal">Annuler</button>
            <button type="submit" class="btn btn-primary" id="submitEditUser" data-loading-text="Loading...">Enregistrer</button>
          </div>
      </form>
      <script type="text/javascript">
      $("form").submit(function(e){
        e.preventDefault();
        var btn = $("#submitEditUser");
        btn.button('loading');
        $.ajax({
                url: '/api/user/update/'+$("#user_id").val(),
                type: 'PUT',
                headers:{
                    "X-API-KEY":"5422e102a743fd70a22ee4ff7c2ebbe8",
                    "Content-Type":"application/x-www-form-urlencoded"
                },
                data:{id:$("#user_id").val(),email:$("#user_email").val(), is_admin:$("input[name=user_is_admin]:checked").val()},
                success: function(result) {
                    if(result.error == false){
                        $("div.result").append('<p class="bg-success" style="padding: 5px 0px;">'+result["message"]+'</p>');
                    }else{
                        $("div.result").append('<p class="bg-danger" style="padding: 5px 0px;">'+result["message"]+'</p>');
                    }
                    $("#editUser").modal("hide");
                },
                 error: function(result) {
                        $("div.result").append('<p class="bg-danger" style="padding: 5px 0px;">Erreur lors de l\'edit.</p>');
                        $("#editUser").modal("hide");
                }
        }).done(function(){
            setTimeout(function() {
                location.reload();
            }, 3000);
        }).always(function() {
              btn.button('reset');
               editencours = false;
            });
      });
      </script>
    </div><!-- /.modal-content -->
  </div><!-- /.modal-dialog -->
</div><!-- /.modal -->