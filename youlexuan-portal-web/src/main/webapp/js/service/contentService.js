//服务层
app.service('contentService',function($http){
	//根据广告分类ID查询广告
	this.findByCategoryId=function(categoryId){
		return $http.get('content/findByCategoryId.do?categoryId='+categoryId);
	}
	//读取列表数据绑定到表单中
	this.findAll=function(){
		return $http.get('../content/findAll.do');		
	}

});