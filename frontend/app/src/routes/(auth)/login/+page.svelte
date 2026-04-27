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

{#if form?.msg && (form?.error || form?.authError)}
	<div class="mb-2">
		<ErrorMessage msg={form.msg} bind:nr={errNr} />
	</div>
{/if}
<AuthCard>
	<AuthHeader to="signup" title={m.user_login()} prompt={m.user_prompt_no_account()} />
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
		<AuthInputField id="email" label={m.user_email()}>
			<input
				name="email"
				type="email"
				value={form?.email ?? ''}
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
				value={form?.authError && ''}
				required
				placeholder={m.user_password()}
				maxlength="256"
				autocomplete="current-password"
			/>
		</AuthInputField>
		<AuthButton>{m.user_login()}</AuthButton>
	</form>
</AuthCard>
