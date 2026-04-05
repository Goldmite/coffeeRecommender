<script lang="ts">
	let { children } = $props();
</script>

<div class="flex w-full flex-1 items-center justify-center">
	<div class="sm:max-w-[400px]">
		{@render children()}
	</div>
</div>
