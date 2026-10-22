/**
 *@author Rakhi Sharma
 *@description JavaScript functions for expense management
 */
$(document).ready(function () {
    $.ajax({
        url: "/api/categories/all",
        type: "GET",
        dataType: "json",

        success: function (data) {
            if (data.status === "OK" && data.statusCode === 1) {
                const categoryDropdown = $("#categoryId");
                categoryDropdown.empty();
                categoryDropdown.append('<option value="">Select category</option>');
                $.each(data.response, function (index, category) {
                    categoryDropdown.append(
                        $("<option>", {value: category.categoryId,text: category.name})
                    );

                });

            } else {
                console.log("No categories found.");
            }
        },
		error: function(xhr, status, error) {
			console.error("AJAX status:", status);
			console.error("HTTP status:", xhr.status);
			console.error("Error:", error);
			console.error("Response:", xhr.responseText);
		}
    });
	
	getExpenseList();
});

$("#categoryId").on("change", function(){
	var categoryId = $("#categoryId").val();
	var url = "/api/subcategory/category/"+categoryId;
	console.log("URL : ", url);
	
	const subcategoryDropdown = $("#subcategoryId");

	// Reset previous subcategories
	subcategoryDropdown.empty();
	/*subcategoryDropdown.append(
		'<option value="">Select subcategory</option>'
	);*/

	// Disable dropdown if no category is selected
	if (!categoryId) {
		subcategoryDropdown.prop("disabled", true);
		return;
	}
	
	$.ajax({
		url:url,
		type: "GET",
		dataType:"json",
		success: function(data){
		
			if(data.status === "OK" && data.statusCode === 1){
				if (data.response && data.response.length > 0) {
					$.each(data.response, function(index, subcategory) {

						subcategoryDropdown.append(
							$("<option>", {
								value: subcategory.subCategoryId,
								text: subcategory.name
							})
						);

					});
					subcategoryDropdown.prop("disabled", false);
				}else{
					subcategoryDropdown.append(
						'<option value="">No subcategories available</option>'
					);
				}
				
			}
		},
		error: function(xhr) {
			console.error(
				"Error fetching subcategories:",
				xhr.responseText
			);
		}
	});
});

function addExpense(){
	const title = $("#expenseTitle").val().trim();
	const amount = $("#expenseAmount").val();
	const categoryId = $("#categoryId").val();
	const subCategoryId = $("#subcategoryId").val();
	const expenseDate = $("#expenseDate").val();
	const paymentMethod = $("#paymentMethod").val();
	const description = $("#expenseDescription").val().trim();
	
	if (!title || !amount || !categoryId ||
		!subCategoryId || !expenseDate || !paymentMethod) {

		alert("Please fill in all required fields.");
		return;
	}

	if (!Number.isFinite(Number(amount)) || Number(amount) <= 0) {
		alert("Please enter a valid amount.");
		return;
	}
	const csrfToken = $("meta[name='_csrf']").attr("content");
	const csrfHeader = $("meta[name='_csrf_header']").attr("content");

	const headers = {};

	if (csrfToken && csrfHeader) {
		headers[csrfHeader] = csrfToken;
	}
	
	$.ajax({
		url: "/api/expense/save",
		type: "POST",
		headers: headers,
		data:{
			title: title,
			amount: amount,
			categoryId:categoryId,
			subCategoryId:subCategoryId,
			expenseDate: expenseDate,
			paymentMethod: paymentMethod,
			description: description
		},
		success: function(data){
			if(data.status === "OK" && data.statusCode === 1){
				Swal.fire({
					icon: "success",
					title: "Success!",
					text: data.responseMessage || "Expense saved successfully.",
					confirmButtonText: "OK",
					confirmButtonColor: "#3085d6"
				});
				location.reload();
			}else{
				Swal.fire({
					icon: "error",
					title: "Failed!",
					text: data.responseMessage || "Unable to save expense.",
					confirmButtonText: "OK"
				});
			}
		},
		error: function(xhr){
			const message = xhr.responseJSON?.responseMessage || "An error occurred while saving the expense.";
			alert(message);
			console.error(xhr.responseText);
		}
		
	});
}

function getExpenseList(){
	$("#viewExpenseModal").on("show.bs.modal", function (event) {
	        const button = $(event.relatedTarget);
	        $("#vExpenseTitle").text(
	            button.attr("data-title") || "-"
	        );
	        $("#vExpenseDate").text(
	            button.attr("data-date") || "-"
	        );
	        const amount = parseFloat(button.attr("data-amount"));
	        $("#vExpenseAmount").text(
	            Number.isFinite(amount)
	                ? amount.toLocaleString("en-IN", {
	                    style: "currency",
	                    currency: "INR"
	                })
	                : "-"
	        );
	        $("#vExpenseCategory").text(
	            button.attr("data-category") || "-"
	        );

	        $("#vExpenseDescription").text(
	            button.attr("data-description") || "-"
	        );
	        const paymentMethod = button.attr("data-payment") || "";
			$("#vExpensePayment").text(
			    paymentMethod
			        ? paymentMethod.replace(/_/g, " ")
			                       .toLowerCase()
			                       .replace(/\b\w/g, char => char.toUpperCase())
			        : "-"
			);
	  });
}

let expenseIdToDelete = null;

// Capture expenseId when modal opens
$("#deleteExpenseModal").on("show.bs.modal", function (event) {
    const deleteButton = $(event.relatedTarget);

    expenseIdToDelete = deleteButton.attr("data-expenseid");

    console.log("Expense ID to delete:", expenseIdToDelete);
});

// Confirm deletion
$("#confirmDeleteBtn").on("click", function () {
    if (expenseIdToDelete !== null) {
		
        remove(expenseIdToDelete);
    }
});

function remove(expenseId){
	const deleteButton = $("#confirmDeleteBtn");

	deleteButton.prop("disabled", true);
	const csrfToken = $("meta[name='_csrf']").attr("content");
	const csrfHeader = $("meta[name='_csrf_header']").attr("content");
	$.ajax({
		url:"/api/expense/remove/"+expenseId,
		type:"DELETE",
		beforeSend: function(xhr) {
			xhr.setRequestHeader(csrfHeader, csrfToken);
		},
		success:function(data){
			if(data.status === "SUCCESS" && data.statusCode === 1){
				// Close Bootstrap modal
				const modalElement = document.getElementById("deleteExpenseModal");
				bootstrap.Modal.getOrCreateInstance(modalElement).hide();
				
				Swal.fire({
					title: "Success!",
					text: data.responseMessage || "Expense deleted successfully.",
					icon: "success"
				}).then(() => {
					location.reload();
				});
			}else{
				Swal.fire(
					"Failed!",
					data.responseMessage || "Unable to delete expense.",
					"error"
				);
			}
		},
		error:function(xhr){
			const message =
				xhr.responseJSON?.responseMessage ||
				"An error occurred while deleting the expense.";

			Swal.fire("Error!", message, "error");
		}
	});
}