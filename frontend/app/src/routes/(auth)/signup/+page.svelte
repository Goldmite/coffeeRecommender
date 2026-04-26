<script lang="ts">
	import { enhance } from '$app/forms';
	import AuthButton from '$lib/components/core/SubmitButton.svelte';
	import AuthCard from '$lib/components/auth/AuthCard.svelte';
	import AuthHeader from '$lib/components/auth/AuthHeader.svelte';
	import AuthInputField from '$lib/components/auth/AuthInputField.svelte';
	import ErrorMessage from '$lib/components/core/ErrorMessage.svelte';
	import { m } from '$lib/paraglide/messages.js';
	let { form } = $props();
	let errNr = $state(0);
</script>

{#if form?.msg && (form?.error || form?.conflictError)}
	<div class="mb-2">
		<ErrorMessage msg={form.msg} bind:nr={errNr} />
	</div>
{/if}
<AuthCard>
	<AuthHeader to="login" title={m.user_signup()} prompt={m.user_prompt_has_account()} />
	<form
		class="flex flex-col gap-3"
		method="POST"
		use:enhance={() => {
			return async ({ result, update }) => {
				await update();

				if (result.type === 'failure') {
					errNr += 1;
				} else {
					errNr = 0;
				}
			};
		}}
	>
		<AuthInputField id="name" label={m.user_name()}>
			<input
				name="name"
				type="text"
				value={form?.name ?? ''}
				required
				placeholder={m.user_name()}
				minlength="3"
				maxlength="24"
			/>
		</AuthInputField>
		<AuthInputField id="email" label={m.user_email()}>
			<input
				name="email"
				type="email"
				value={form?.conflictError && ''}
				required
				placeholder={m.user_email()}
				maxlength="256"
				autocomplete="email"
			/>
		</AuthInputField>
		<AuthInputField id="password" label={m.user_password()}>
			<input
				name="password"
				type="password"
				required
				placeholder={m.user_password()}
				minlength="10"
				maxlength="256"
				autocomplete="new-password"
				pattern="^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&]).+$"
				title={m.auth_prompt_password_format()}
			/>
		</AuthInputField>
		<AuthButton>{m.user_signup()}</AuthButton>
	</form>
</AuthCard>
