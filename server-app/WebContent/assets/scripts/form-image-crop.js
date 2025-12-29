var crop_x=0, crop_y=0, crop_w=0, crop_h=0;
var FormImageCrop = function () {

    
	var jcrop_api=null;
    var demo2 = function() {
        

        $('#input_img').Jcrop({
          onChange:   showCoords,
          onSelect:   showCoords,
          onRelease:  clearCoords
        },function(){
          jcrop_api = this;		  
        });

        /*$('#coords').on('change','input',function(e){
          var x1 = $('#x1').val(),
              x2 = $('#x2').val(),
              y1 = $('#y1').val(),
              y2 = $('#y2').val();
          jcrop_api.setSelect([x1,y1,x2,y2]);
        });*/

        // Simple event handler, called from onChange and onSelect
        // event handlers, as per the Jcrop invocation above
        function showCoords(c)
        {
			crop_x=c.x; crop_y=c.y; crop_w=c.w; crop_h=c.h;
            /*$('#x1').val(c.x);
            $('#y1').val(c.y);
            $('#x2').val(c.x2);
            $('#y2').val(c.y2);
            $('#w').val(c.w);
            $('#h').val(c.h);*/
        };

        function clearCoords()
        {
            //$('#coords input').val('');
			crop_x=0; crop_y=0; crop_w=0; crop_h=0;
        };
		crop_x=0; crop_y=0; crop_w=0; crop_h=0;
    }

    var handleResponsive = function() {
      if ($(window).width() <= 1024 && $(window).width() >= 678) {
        $('.responsive-1024').each(function(){
          $(this).attr("data-class", $(this).attr("class"));
          $(this).attr("class", 'responsive-1024 col-md-12');
        }); 
      } else {
        $('.responsive-1024').each(function(){
          if ($(this).attr("data-class")) {
            $(this).attr("class", $(this).attr("data-class"));  
            $(this).removeAttr("data-class");
          }
        });
      }
    }

    return {
        //main function to initiate the module
        init: function () {
            if (!jQuery().Jcrop) {;
                return;
            }
			if (jcrop_api!=null) jcrop_api.destroy();
            //App.addResponsiveHandler(handleResponsive);
            //handleResponsive();

            demo2();
            
        }

    };

}();