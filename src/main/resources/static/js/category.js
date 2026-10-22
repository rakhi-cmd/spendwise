/**
 * @author Rakhi Sharma
 * @description Javascript function to manage dynamic data in dropdowns
 */

$(document).on("click", ".add-subcategory-btn", function() {

		const categoryId = $(this).data("category-id");

		$("#addSubcategoryCategory").val(categoryId);
		// Submit category ID
	    $("#addSubcategoryCategoryId").val(categoryId);
		$("#addSubcategoryName").val("");
		$("#addSubcategoryDescription").val("");
		$("#addSubcategoryStatus").val("1");

		const modalElement = document.getElementById("addSubcategoryModal");
		const modal = bootstrap.Modal.getOrCreateInstance(modalElement);

		modal.show();
	});
	
	$("#addSubcategoryForm").on("submit", function(e) {
	    e.preventDefault();
	    
	    const csrfToken = $("meta[name='_csrf']").attr("content");
	    const csrfHeader = $("meta[name='_csrf_header']").attr("content");
	    
	    $.ajax({
	        url: "/api/subcategory/save",
	        type: "POST",
	        data: $(this).serialize(),
	        beforeSend: function(xhr) {
	            xhr.setRequestHeader(csrfHeader, csrfToken);
	        },
	        success: function(response) {
	            if (response.status === "OK" && response.statusCode === 1) {
	                Swal.fire(
	                    "Success",
	                    response.responseMessage,
	                    "success"
	                ).then(() => {
	                    const modalElement = document.getElementById("addSubcategoryModal");
	                    const modal = bootstrap.Modal.getInstance(modalElement);
	                    if (modal) {
	                        modal.hide();
	                    }
	                    location.reload();
	                });
	            } else {
	                Swal.fire(
	                    "Error",
	                    response.responseMessage,
	                    "error"
	                );
	            }
	        },
	        error: function(xhr) {
	            console.error(xhr);
	            Swal.fire(
	                "Error",
	                "Unable to save subcategory",
	                "error"
	            );
	        }
	    });
	});
	
	$('#statusCategoryModal').on('show.bs.modal', function (event) {

	    const button = $(event.relatedTarget);
	    const categoryId = button.attr('data-category-id');
	    $('#statusCategoryId').val(categoryId);
	    $.ajax({
	        url: '/api/categories/status-info/' + categoryId,
	        type: 'GET',
	        success: function (data) {
	            debugger;
	            if (data.status === "OK" && data.statusCode === 1) {
	                const category = data.response;
	                console.log("Category status:", category.status);

	                // CATEGORY IS ACTIVE
	                if (Number(category.status) === 1) {
	                    $('#statusCategoryTitle').text('Deactivate Category?');
	                    $('#confirmCategoryStatus').text('Deactivate');
	                    if (category.subCategoryCount > 0) {
	                        $('#statusCategoryMessage').html(
	                            'This category has <strong>' +
	                            category.subCategoryCount +
	                            '</strong> subcategor' +
	                            (category.subCategoryCount === 1
	                                ? 'y'
	                                : 'ies') +

	                            '.<br><br>' +

	                            'Deactivating this category will make it ' +
	                            'unavailable for new expenses. ' +

	                            'Existing expenses will not be affected.'
	                        );

	                    } else {

	                        $('#statusCategoryMessage').text(

	                            'This category will no longer be available ' +
	                            'when adding new expenses.'
	                        );
	                    }

	                }

	                // CATEGORY IS INACTIVE
	                else {

	                    $('#statusCategoryTitle').text('Activate Category?');

	                    $('#confirmCategoryStatus').text('Activate');

	                    $('#statusCategoryMessage').text(

	                        'This category will become available again ' +
	                        'when adding new expenses.'
	                    );
	                }

	            } else {

	                $('#statusCategoryMessage').text(
	                    'Unable to get category information.'
	                );
	            }
	        },

	        error: function (xhr) {

	            console.error("Error:", xhr);

	            $('#statusCategoryMessage').text(
	                'Unable to check category information.'
	            );
	        }
	    });

	});
	
	$('#confirmCategoryStatus').on('click', function () {
		const csrfToken = $("meta[name='_csrf']").attr("content");
	    const csrfHeader = $("meta[name='_csrf_header']").attr("content");
	    const categoryId = $('#statusCategoryId').val();
	    $.ajax({
	        url: '/api/categories/status/' + categoryId,
	        type: 'POST',
	        beforeSend: function(xhr) {
	            xhr.setRequestHeader(csrfHeader, csrfToken);
	        },
	        success: function (data) {
	            if (data.status === "OK" && data.statusCode === 1) {
	                $('#statusCategoryModal').modal('hide');
	            	Swal.fire({
	            	    icon: 'success',
	            	    title: 'Success',
	            	    text: 'Category status changed successfully.',
	            	    confirmButtonText: 'OK'
	            	}).then(function () {
	            	    location.reload();
	            	});
	            } else {
	                swal(
	                    "Error",
	                    data.responseMessage ||
	                    "Unable to change category status.",
	                    "error"
	                );
	            }
	        },
	        error: function (xhr) {
	            console.error("Category status update error:", xhr);
	            swal(
	                "Error",
	                "Unable to change category status.",
	                "error"
	            );
	        }
	    });
	});
	
	function getCategory(categoryId){
		$.ajax({
			url: "/api/categories/get/"+categoryId,
			type:'GET',
			success: function(data){
				if(data.status === "OK" && data.statusCode === 1){
					var category = data.response;
					document.getElementById('editCategoryId').value = category.categoryId;
					document.getElementById('editCategoryName').value = category.name;
					document.getElementById('editCategoryDescription').value = category.description;
					document.getElementById('editCategoryStatus').value = category.status;
				}
			},
			error: function(xhr){
				console.error(xhr.responseText);
			}
		});
	}
	

	function updateCategory() {

	    const csrfToken = $("meta[name='_csrf']").attr("content");
	    const csrfHeader = $("meta[name='_csrf_header']").attr("content");

	    const categoryId = $("#editCategoryId").val();
	    const name = $("#editCategoryName").val();
	    const description = $("#editCategoryDescription").val();
	    const status = $("#editCategoryStatus").val();

	    $.ajax({
	        url: "/api/categories/update",
	        type: "POST",
	        beforeSend: function(xhr) {
	            xhr.setRequestHeader(csrfHeader, csrfToken);
	        },
	        data: {
	            categoryId: categoryId,
	            name: name,
	            description: description,
	            status: parseInt(status)
	        },
	        success: function(data) {
	            if (data.status === "OK" && data.statusCode === 1) {
	                Swal.fire({
	                    icon: "success",
	                    title: "Success",
	                    text: data.responseMessage || "Category updated successfully.",
	                    confirmButtonText: "OK"
	                }).then(function() {
	                    location.reload();
	                });

	            } else {
	                Swal.fire({
	                    icon: "error",
	                    title: "Error",
	                    text: data.responseMessage || "Unable to update category."
	                });
	            }
	        },

	        error: function(xhr) {
	            console.error("Update category error:", xhr.responseText);

	            Swal.fire({
	                icon: "error",
	                title: "Error",
	                text: "Unable to update category."
	            });
	        }
	    });
	}
	
	function getSubcategoryData(subcategoryId){
		$.ajax({
			url: "/api/subcategory/get/"+subcategoryId,
			type: "GET",
			success: function(data){
				if(data.status === "OK" && data.statusCode === 1){
					$("#editSubcategoryCategory").val(data.response.categoryId);
					$("#editSubcategoryName").val(data.response.name);
					$("#editSubcategoryDescription").val(data.response.description || " ");
					$("#editSubcategoryStatus").val(data.response.status);
					$("#editSubcategoryId").val(String(data.response.categoryId));
				}
			},
			error: function(xhr){
				console.error(xhr.responseText);
			}
		});
	}