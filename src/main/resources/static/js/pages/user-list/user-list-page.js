import { openConfirmationModal } from '../home/modal.js';


function initRoleActions() {
    document.querySelectorAll('[data-user-action]')
        .forEach(button => {
            button.addEventListener('click', function(event) {
                event.preventDefault();

                const form = document.getElementById("userActionForm");
                const selectedUsers = Array.from(
                    form.querySelectorAll(
                        'input[name="selectedUsers"]:checked'
                    )
                );

                if (selectedUsers.length === 0) {
                    showToast(
                        "Please select at least one user",
                        "error"
                    );
                    return;
                }

                openConfirmationModal({
                    title: `Confirm ${this.dataset.userAction}`,
                    message: 
                        `Are you sure you want to ${this.dataset.userAction} ${selectedUsers.length} user(s)?`,
                    confirmText: "Confirm",

                    onConfirm: async () => {
                        const role = this.dataset.role;

                        if (role) {
                            let roleInput = form.querySelector('input[name="role"]');

                            if (!roleInput) {
                                roleInput = document.createElement("input");
                                roleInput.type = "hidden";
                                roleInput.name = "role";
                                form.appendChild(roleInput);
                            }

                            roleInput.value = role;
                        }

                        form.action = this.dataset.endpoint;

                        console.log("Submitting:", {
                            endpoint: form.action,
                            role: form.querySelector('input[name="role"]')?.value,
                            users: selectedUsers.map(u => u.value)
                        });

                        form.submit();
                    }
                });
            });
        });
}

initRoleActions();