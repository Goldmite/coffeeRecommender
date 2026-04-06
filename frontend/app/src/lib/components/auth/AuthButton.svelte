<script lang="ts">
	let { btnText, disabled = false } = $props();
</script>

<button class="btn-wide mt-4 bg-primary tracking-wide" type="submit" {disabled}>{btnText}</button>

<style>
	button[type='submit']:hover {
		box-shadow: var(--inset-shadow-md);
	}
</style>
