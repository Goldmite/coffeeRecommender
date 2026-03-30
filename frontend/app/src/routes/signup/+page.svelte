<script lang="ts">
	import { signup } from '$lib/api/auth';

    let name = '';
	let email = '';
	let password = '';

	async function handleSubmit() {
		try {
			const user = await signup({ name, email, password });
			console.log(user);
		} catch (error) {
			console.log(error);
		}
	}
</script>

<h1>Login</h1>

<form on:submit|preventDefault={handleSubmit}>
	<input bind:value={name} placeholder="Name" />
	<input type="email" bind:value={email} placeholder="Email" />
	<input type="password" bind:value={password} placeholder="Password" />
	<button type="submit">Signup</button>
</form>