<script lang="ts">
	let { title, prompt, to } = $props();
</script>

<div class="mb-4 flex justify-between">
	<h1 class="w-full">{title}</h1>
	<a href={to} class="pt-2 text-sm font-light underline">{prompt}</a>
</div>
