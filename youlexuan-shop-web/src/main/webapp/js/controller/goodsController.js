 //控制层 
app.controller('goodsController' ,function($scope,$controller,$location,goodsService,uploadService,itemCatService,typeTemplateService){

	$controller('baseController',{$scope:$scope});//继承

		// 定义页面实体结构
		$scope.entity={goods:{},
			goodsDesc:{
				itemImages:[],
				specificationItems : []
			}

		};



	//创建SKU列表
	$scope.createItemList=function(){
		//定义 数组
		$scope.entity.itemList=[{spec:{},price:0,num:99999,status:'0',isDefault:'0' }];//初始
		//勾选的SKU的对象
		var items=  $scope.entity.goodsDesc.specificationItems;
		for(var i=0;i< items.length;i++){
			$scope.entity.itemList = addColumn($scope.entity.itemList,items[i].attributeName,items[i].attributeValue );
		}

	};
	//添加列值
	addColumn=function(list,columnName,columnValues){

		var newList=[];//新的集合

		for(var i=0;i<list.length;i++){
			//{spec:{},price:0,num:99999,status:'0',isDefault:'0' }
			var oldRow= list[i];
			//["移动3G","移动4G"]
			for(var j=0;j<columnValues.length;j++){
				var newRow= JSON.parse(JSON.stringify(oldRow));//深克隆
				//       网络       移动3G"
//          spec:{[网络：移动3G]}
				newRow.spec[columnName]=columnValues[j];
//          var newList=[[网络：移动3G],[网络：移动4G]];
				newList.push(newRow);
			}
		}
		return newList;
	}
	//entity.goodsDesc.specificationItems=[{“attributeName”:”网络”,”attributeValue”:[“移动3G”] }];
	$scope.updateSpecAttribute = function($event, name, value) {
		var object=$scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems,'attributeName',name);
		if (object != null) {
			if ($event.target.checked){
				//勾选情况
				object.attributeValue.push(value);
			}else {
				// 取消勾选
				object.attributeValue.splice(object.attributeValue.indexOf(value ) ,1);//移除选项
				// 如果选项都取消了，将此条记录移除
				if (object.attributeValue.length == 0) {
					$scope.entity.goodsDesc.specificationItems.splice(
						$scope.entity.goodsDesc.specificationItems
							.indexOf(object), 1);
				}
			}
		}else {
			$scope.entity.goodsDesc.specificationItems.push({
				"attributeName" : name,
				"attributeValue" : [ value ]
			});
		}
	}

// 从集合中按照key查询对象
	$scope.searchObjectByKey=function(list, key, keyValue){
		for (var i = 0; i < list.length; i++) {
			if (list[i][key] == keyValue) {
				return list[i];
			}
		}
		return null;
	};

	//查询一级标题
	$scope.selectItemCat1List=function(){
		itemCatService.findByParentId(0).success(function(response){
			$scope.itemCat1List=response
		})
	};
	//查询二级标题
	$scope.$watch('entity.goods.category1Id',function (newValue,oldValue) {
		//根据选择的值查询二级分类
		itemCatService.findByParentId(newValue).success(function (response) {
			$scope.itemCat2List=response;
		})
	})

	//读取三级分类
	$scope.$watch('entity.goods.category2Id', function(newValue, oldValue) {
		//根据选择的值，查询二级分类
		itemCatService.findByParentId(newValue).success(
			function(response){
				$scope.itemCat3List=response;
			}
		);
	});

	//查询三级标题后 更细模板
	$scope.$watch('entity.goods.category3Id',function (newValue, oldValue) {
		itemCatService.findOne(newValue).success(function (response) {
			$scope.entity.goods.typeTemplateId=response.typeId; //更新模板ID
		})
	})

	//模板ID选择后  更新品牌列表
	$scope.$watch('entity.goods.typeTemplateId', function(newValue, oldValue) {
		typeTemplateService.findOne(newValue).success(function (response) {
			$scope.typeTemplate=response;//获取类型模板
			$scope.typeTemplate.brandIds= JSON.parse( $scope.typeTemplate.brandIds);//品牌列表
			if($location.search()['id']==null) {
				$scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.typeTemplate.customAttributeItems);//扩展属性
			}
		})
		//查询规格列表
		typeTemplateService.findSpecList(newValue).success(function (response) {
			$scope.specList=response;
		})
	});
	$scope.status=['未审核','已审核','审核未通过','关闭'];//商品状态
	$scope.itemCatList=[];//商品分类列表
	//加载商品分类列表
	$scope.findItemCatList=function(){
		itemCatService.findAll().success(
			function(response){
				for(var i=0;i<response.length;i++){
					//id做为下标 名称作为内容
					$scope.itemCatList[response[i].id]=response[i].name;
				}
			}
		);
	}

	//列表中移除图片
	$scope.remove_image_entity=function(index){

		$scope.entity.goodsDesc.itemImages.splice(index,1);
	};

	//添加图片列表
	$scope.add_image_entity=function(){
		$scope.entity.goodsDesc.itemImages.push($scope.image_entity);
	};

	$scope.uploadFile=function(){
		uploadService.uploadFile().success(function(response){
			if(response.success){//如果上传成功，取出url
				$scope.image_entity.url=response.message;//设置文件地址
			}else{
				alert(response.message);
			}
		}).error(function() {
			alert("上传发生错误");
		});
	};

	$scope.add=function(){
		$scope.entity.goodsDesc.introduction=editor.html();
		var serviceObject;// 服务层对象
		if ($scope.entity.goods.id != null) {// 如果有ID
			serviceObject = goodsService.update($scope.entity);
		} else {
			serviceObject = goodsService.add($scope.entity);// 增加
		}
		serviceObject.success(
			function(response){
				if(response.success){
					alert(response.message);
					$scope.entity={};
					editor.html('');//清空富文本编辑器
					location.href="goods.html";//跳转到商品列表页
				}else{
					alert(response.message);
				}
			}
		);
	}

    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    

	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(){
		var id= $location.search()['id'];//获取参数值
		if(id==null){
			return ;
		}
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;
				//向富文本编辑器添加商品介绍
				editor.html($scope.entity.goodsDesc.introduction);
				//显示图片列表
				$scope.entity.goodsDesc.itemImages=
					JSON.parse($scope.entity.goodsDesc.itemImages);

					//显示扩展属性
				$scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.entity.goodsDesc.customAttributeItems);
				//规格
				$scope.entity.goodsDesc.specificationItems=JSON.parse($scope.entity.goodsDesc.specificationItems);

				//SKU信息
				for( var i=0;i<$scope.entity.itemList.length;i++ ){
					$scope.entity.itemList[i].spec =
						JSON.parse( $scope.entity.itemList[i].spec);
				}
			}
		);				
	};
	//根据规格名称和选项名称返回是否被勾选
	$scope.checkAttributeValue=function(specName,optionName){
		var items= $scope.entity.goodsDesc.specificationItems;//回显
		//console.log(items);
		var object= $scope.searchObjectByKey(items,'attributeName',specName);
		if(object==null){
			return false;
		}else{
			if(object.attributeValue.indexOf(optionName)>=0){
				return true;
			}else{
				return false;
			}
		}
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        	$scope.reloadList();//重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					alert(response.message);
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
    
});	