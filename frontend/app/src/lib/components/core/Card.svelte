<script lang="ts">
	let { children } = $props();
</script>

<div
	class="overflow-hidden rounded-tl-2xl rounded-tr-lg rounded-br-2xl rounded-bl-lg border border-main-border"
>
	{@render children()}
</div>
