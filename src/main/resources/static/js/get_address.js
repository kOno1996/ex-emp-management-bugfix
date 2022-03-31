'use strict';
$(function(){
	//住所取得ボタンクリックで非同期通信を行う
	$(document).on('click','#get_address_btn',function(){
			console.log($('#zipCode').val())
		$.ajax({
			url:'https://zipcoda.net/api',
			dataType: 'jsonp',
			data:{
				zipcode:$('#zipCode').val()
			},
			async: true
		}).done(function(data){
			console.dir(JSON.stringify(data));
			$('#address').val(data.items[0].address);
		}).fail(function(XMLHttpRequest, textStatus, errorThrown){
			alert('正しい結果を得られませんでした');
		});
	});
});